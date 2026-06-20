package ai.codegeist.app.tool;

import ai.codegeist.app.chat.CodegeistChatExecutionContext;
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
 * <p>For this slice the service assembles local read/list/glob/grep/write callbacks
 * and shares one ordered recorder with them. MCP callbacks and resource cleanup are
 * intentionally absent until T007_03_05 adds the first resource-owning tool run.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodegeistToolService {

    private final CodegeistLocalTools localTools;

    public CodegeistToolRun openRun(@NonNull Path workingDirectory) {
        List<ToolSessionPart> completedToolParts = new ArrayList<>();
        List<ToolCallback> callbacks = localTools.callbacks(completedToolParts::add);
        log.debug("Opened Codegeist tool run in {} with {} local callbacks", workingDirectory, callbacks.size());
        return new DefaultCodegeistToolRun(
                new CodegeistChatExecutionContext(workingDirectory, callbacks),
                completedToolParts);
    }
}
