package ai.codegeist.app.mcp;

import java.util.List;
import org.springframework.ai.tool.ToolCallback;

/**
 * Prompt-scoped MCP callbacks and resources.
 *
 * <p>Opening a run may start stdio processes or remote HTTP client sessions, so the
 * chat harness must close the owning {@code CodegeistToolRun} after each provider
 * call. The run exposes callbacks only; MCP client definitions and runtime status do
 * not enter the session store.
 */
public interface CodegeistMcpRun extends AutoCloseable {

    List<ToolCallback> getToolCallbacks();

    @Override
    void close();
}
