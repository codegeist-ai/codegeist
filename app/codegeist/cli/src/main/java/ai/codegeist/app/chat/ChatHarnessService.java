package ai.codegeist.app.chat;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.ProviderConfig;
import ai.codegeist.app.session.SessionStoreService;
import ai.codegeist.app.tool.CodegeistToolRun;
import ai.codegeist.app.tool.CodegeistToolService;
import ai.codegeist.app.tool.WorkspaceResolver;
import java.nio.file.Path;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orchestrates one non-streaming chat turn for command and future UI callers.
 *
 * <p>The service keeps provider selection, prompt-scoped tool callback setup,
 * provider invocation, and session-store persistence in one boundary while leaving
 * stdout handling to Spring Shell adapters. It intentionally does not rebuild
 * provider-facing context from stored session history; persisted sessions only record
 * the prompt, bounded tool activity, and assistant response for this T007 slice.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHarnessService {

    private final CodegeistConfig config;

    private final CodegeistChatService chatService;

    private final CodegeistToolService toolService;

    private final WorkspaceResolver workspaceResolver;

    private final SessionStoreService sessionStoreService;

    public CodegeistChatResponse ask(boolean continueSession, @NonNull String prompt) {
        ProviderConfig providerConfig = config.defaultProvider()
                .orElseThrow(() -> new IllegalStateException(CodegeistConfig.NO_PROVIDER_MESSAGE));
        String model = providerConfig.defaultModel();
        Path workingDirectory = workspaceResolver.currentWorkspace();
        log.debug("Asking provider type {} with default model {} and tools", providerConfig.getType(), model);
        CodegeistToolRun toolRun = toolService.openRun(workingDirectory);
        CodegeistChatResponse response = chatService.chat(
                providerConfig,
                new CodegeistChatRequest(model, prompt),
                toolRun.executionContext());
        sessionStoreService.saveExchangeToCurrentSession(
                continueSession,
                prompt,
                response.content(),
                toolRun.completedToolParts());
        return response;
    }
}
