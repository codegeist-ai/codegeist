package ai.codegeist.app.tool;

import ai.codegeist.app.chat.CodegeistChatExecutionContext;
import ai.codegeist.app.session.ToolSessionPart;
import java.util.List;

/**
 * Per-turn scope for chat tool callbacks and recorded tool activity.
 *
 * <p>The scope now also owns MCP resources created for the current chat turn. The
 * chat harness must close each run after the provider call so stdio processes and
 * remote MCP client sessions are released promptly.
 */
public interface CodegeistToolRun extends AutoCloseable {

    CodegeistChatExecutionContext executionContext();

    List<ToolSessionPart> completedToolParts();

    @Override
    default void close() {
    }
}
