package ai.codegeist.app.mcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;

/**
 * Default prompt-scoped MCP run implementation.
 *
 * <p>The run exposes the callbacks discovered for the prompt and closes created MCP
 * resources in reverse order. Cleanup failures are logged and suppressed so close
 * does not mask a provider-call failure from the original chat turn.
 */
@Slf4j
@RequiredArgsConstructor
final class DefaultCodegeistMcpRun implements CodegeistMcpRun {

    @Getter
    private final List<ToolCallback> toolCallbacks;

    private final List<AutoCloseable> closeables;

    static DefaultCodegeistMcpRun empty() {
        return new DefaultCodegeistMcpRun(List.of(), List.of());
    }

    static DefaultCodegeistMcpRun fromHandles(List<CodegeistMcpClientHandle> handles) {
        List<ToolCallback> callbacks = new ArrayList<>();
        List<AutoCloseable> resources = new ArrayList<>();
        for (CodegeistMcpClientHandle handle : handles) {
            callbacks.addAll(handle.toolCallbacks());
            resources.add(handle.closeable());
        }
        return new DefaultCodegeistMcpRun(List.copyOf(callbacks), List.copyOf(resources));
    }

    @Override
    public void close() {
        List<AutoCloseable> resources = new ArrayList<>(closeables);
        Collections.reverse(resources);
        for (AutoCloseable closeable : resources) {
            try {
                closeable.close();
            }
            catch (Exception exception) {
                log.debug("Failed to close MCP resource", exception);
            }
        }
    }
}
