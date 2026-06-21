package ai.codegeist.app.tool;

import ai.codegeist.app.chat.CodegeistChatExecutionContext;
import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.mcp.CodegeistMcpAdapter;
import ai.codegeist.app.mcp.CodegeistMcpRun;
import ai.codegeist.app.session.ToolSessionPart;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

/**
 * Opens one prompt-scoped tool run for Codegeist-owned chat tools.
 *
 * <p>The service assembles local read/list/glob/grep/write callbacks and configured
 * MCP callbacks into one runtime context. Both sources share the same ordered
 * recorder, so persisted {@link ToolSessionPart} values preserve call order without
 * storing enabled tool definitions or MCP client config in the session store.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodegeistToolService {

    private final CodegeistLocalTools localTools;

    private final ToolOutputBounds outputBounds;

    private final CodegeistMcpAdapter mcpAdapter;

    public CodegeistToolRun openRun(@NonNull CodegeistConfig config, @NonNull Path workingDirectory) {
        List<ToolSessionPart> completedToolParts = new ArrayList<>();
        List<ToolCallback> callbacks = new ArrayList<>(localTools.callbacks(completedToolParts::add));
        CodegeistMcpRun mcpRun = mcpAdapter.openRun(config);
        callbacks.addAll(mcpRun.getToolCallbacks().stream()
                .map(callback -> new RecordingToolCallback(callback, outputBounds, completedToolParts::add))
                .toList());
        log.debug(
                "Opened Codegeist tool run in {} with {} callbacks",
                workingDirectory,
                callbacks.size());
        return new DefaultCodegeistToolRun(
                new CodegeistChatExecutionContext(workingDirectory, callbacks),
                completedToolParts,
                mcpRun);
    }
}
