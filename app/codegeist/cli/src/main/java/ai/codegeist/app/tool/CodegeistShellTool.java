package ai.codegeist.app.tool;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;

/**
 * Minimal one-shot local shell callback for the active Codegeist workspace.
 *
 * <p>The shell tool deliberately owns no sandbox, cwd containment, PTY, or
 * background-process manager. It starts one local process through the configured
 * host command prefix, closes stdin, merges stderr into stdout, and returns a
 * shell summary that the callback boundary caps before it reaches the model or
 * session store. A timeout interrupts the future running the process and destroys
 * the child process instead of creating background process state.
 */
@Component
@RequiredArgsConstructor
final class CodegeistShellTool implements CodegeistLocalTool {

    static final String TOOL_NAME = "codegeist_shell";

    private static final long FORCIBLE_DESTROY_WAIT_MILLIS = 500L;
    private static final int TIMEOUT_EXIT_CODE = -1;
    private static final String COMMAND_FIELD = "command";
    private static final String REQUIRED_COMMAND_MESSAGE = "Required text field is missing: command";
    private static final String FAILED_COMMAND_MESSAGE = "Failed to run shell command";
    private static final String INTERRUPTED_COMMAND_MESSAGE = "Shell command interrupted";

    private final CodegeistFileToolSupport support;

    private final CodegeistShellToolSettings settings;

    @Override
    public ToolDefinition definition() {
        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description("Run one local shell command")
                .inputSchema(support.schema("""
                    "command":{"type":"string","description":"Shell command to run once"},
                    "cwd":{"type":"string","description":"Working directory; defaults to ."},
                    "timeoutSeconds":{"type":"integer","description":"Timeout in seconds; defaults to tools.codegeist-shell.default-timeout-seconds"}
                    """, COMMAND_FIELD))
                .build();
    }

    @Override
    public CodegeistToolResult execute(CodegeistToolInput toolInput) {
        Path workspace = support.currentWorkspace();
        ShellToolInput input = support.parseInput(toolInput, ShellToolInput.class);
        String command = support.requireText(input.command(), REQUIRED_COMMAND_MESSAGE);
        Path cwd = support.resolvePath(workspace, support.defaultPath(input.cwd())).toAbsolutePath().normalize();
        ShellExecutionResult result = runProcess(command, cwd, settings.timeoutSeconds(input.timeoutSeconds()));

        return new CodegeistToolResult(summary(workspace, cwd, command, result));
    }

    private ShellExecutionResult runProcess(String command, Path cwd, long timeoutSeconds) {
        ProcessBuilder builder = new ProcessBuilder(shellCommand(command));
        builder.directory(cwd.toFile());
        builder.redirectErrorStream(true);
        AtomicReference<Process> runningProcess = new AtomicReference<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ShellExecutionResult> future = executor.submit(() -> {
            Process process = builder.start();
            runningProcess.set(process);
            process.getOutputStream().close();
            String output;
            try (InputStream processOutput = process.getInputStream()) {
                output = new String(processOutput.readAllBytes(), StandardCharsets.UTF_8);
            }
            return new ShellExecutionResult(process.waitFor(), false, output);
        });

        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        }
        catch (TimeoutException exception) {
            future.cancel(true);
            return new ShellExecutionResult(TIMEOUT_EXIT_CODE, true, "");
        }
        catch (ExecutionException exception) {
            throw new CodegeistToolException(FAILED_COMMAND_MESSAGE, exception);
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            throw new CodegeistToolException(INTERRUPTED_COMMAND_MESSAGE, exception);
        }
        finally {
            stopProcess(runningProcess.get());
            executor.shutdownNow();
        }
    }

    private void stopProcess(Process process) {
        if (process == null || !process.isAlive()) {
            return;
        }
        process.destroy();
        try {
            if (!process.waitFor(FORCIBLE_DESTROY_WAIT_MILLIS, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
            }
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }

    private List<String> shellCommand(String command) {
        List<String> commandLine = new ArrayList<>(settings.commandPrefix());
        commandLine.add(command);
        return commandLine;
    }

    private String summary(Path workspace, Path cwd, String command, ShellExecutionResult result) {
        String separator = CodegeistFileToolSupport.LINE_SEPARATOR;
        String timeoutSummary = result.timedOut() ? separator + "Timed out: true" : "";
        return "Command: " + command
                + separator + "Cwd: " + support.displayPath(workspace, cwd)
                + timeoutSummary
                + separator + "Exit code: " + result.exitCode()
                + separator + "Output:"
                + separator + result.output();
    }

    private record ShellToolInput(String command, String cwd, Long timeoutSeconds) {
    }

    private record ShellExecutionResult(int exitCode, boolean timedOut, String output) {
    }
}
