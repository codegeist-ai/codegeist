package ai.codegeist.app.tool;

import ai.codegeist.app.chat.CodegeistChatExecutionContext;
import ai.codegeist.app.session.ToolSessionPart;
import java.util.List;

/**
 * Per-turn scope for chat tool callbacks and recorded tool activity.
 *
 * <p>The first implementation owns local callbacks only, but keeping the scope
 * explicit lets the next MCP slice add stdio client cleanup when the first closeable
 * resource exists.
 */
public interface CodegeistToolRun {

    CodegeistChatExecutionContext executionContext();

    List<ToolSessionPart> completedToolParts();
}
