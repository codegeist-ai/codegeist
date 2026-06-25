package ai.codegeist.app.chat;

import ai.codegeist.app.config.ProviderConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * Provider-neutral base for Codegeist chat models.
 *
 * <p>Concrete provider models receive access-only provider configuration through the
 * constructor and receive runtime model selection plus message history at call time.
 * Keeping the call contract at {@link CodegeistChatTurnRequest} lets the
 * Codegeist-owned loop replay assistant tool calls and tool results without adding
 * session or tool state to {@link CodegeistChatRequest}.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CodegeistChatModel<T extends ProviderConfig> {

    @Getter(AccessLevel.PROTECTED)
    @NonNull
    private final T providerConfig;

    public abstract ChatResponse call(
            @NonNull CodegeistChatTurnRequest request,
            @NonNull CodegeistChatExecutionContext context);
}
