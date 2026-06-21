package ai.codegeist.app.mcp;

import java.util.List;
import lombok.NonNull;
import org.springframework.ai.tool.ToolCallback;

/**
 * Result of opening one configured MCP client for a prompt-scoped run.
 *
 * <p>The handle keeps Spring AI callback discovery and lifecycle ownership together
 * so {@link CodegeistMcpAdapter} can merge callbacks from multiple configured
 * clients while still closing each created client after the chat turn.
 */
record CodegeistMcpClientHandle(
        @NonNull List<ToolCallback> toolCallbacks,
        @NonNull AutoCloseable closeable) {
}
