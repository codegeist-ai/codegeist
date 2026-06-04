package ai.codegeist.app.chat;

import ai.codegeist.app.config.ProviderConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CodegeistChatModel<T extends ProviderConfig> {

    @Getter(AccessLevel.PROTECTED)
    @NonNull
    private final T providerConfig;

    public abstract ChatResponse call(CodegeistChatRequest request);
}
