package ai.codegeist.app.tool;

import ai.codegeist.app.chat.CodegeistChatExecutionContext;
import ai.codegeist.app.mcp.CodegeistMcpRun;
import ai.codegeist.app.session.ToolSessionPart;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Default prompt-scoped tool-run implementation.
 *
 * <p>The run keeps the mutable recorder list private to the callback scope and
 * returns immutable snapshots to the harness before persistence. It also owns the
 * MCP run so any stdio processes or remote sessions close with the chat turn.
 */
@RequiredArgsConstructor
final class DefaultCodegeistToolRun implements CodegeistToolRun {

    private final CodegeistChatExecutionContext executionContext;

    private final List<ToolSessionPart> completedToolParts;

    private final CodegeistMcpRun mcpRun;

    @Override
    public CodegeistChatExecutionContext executionContext() {
        return executionContext;
    }

    @Override
    public List<ToolSessionPart> completedToolParts() {
        return List.copyOf(completedToolParts);
    }

    @Override
    public void close() {
        mcpRun.close();
    }

}
