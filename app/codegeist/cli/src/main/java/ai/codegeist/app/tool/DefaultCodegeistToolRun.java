package ai.codegeist.app.tool;

import ai.codegeist.app.chat.CodegeistChatExecutionContext;
import ai.codegeist.app.session.ToolSessionPart;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * First local-only tool-run implementation.
 *
 * <p>The run keeps the mutable recorder list private to the callback scope and
 * returns immutable snapshots to the harness before persistence.
 */
@RequiredArgsConstructor
final class DefaultCodegeistToolRun implements CodegeistToolRun {

    private final CodegeistChatExecutionContext executionContext;

    private final List<ToolSessionPart> completedToolParts;

    @Override
    public CodegeistChatExecutionContext executionContext() {
        return executionContext;
    }

    @Override
    public List<ToolSessionPart> completedToolParts() {
        return List.copyOf(completedToolParts);
    }

}
