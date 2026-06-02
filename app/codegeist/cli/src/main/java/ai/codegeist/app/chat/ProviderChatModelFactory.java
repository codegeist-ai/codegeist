package ai.codegeist.app.chat;

import ai.codegeist.app.config.ProviderConfig;
import org.springframework.ai.chat.model.ChatModel;

public interface ProviderChatModelFactory<T extends ProviderConfig> {

    Class<T> configType();

    String providerType();

    ChatModel create(T providerConfig);

    default ChatModel createFrom(ProviderConfig providerConfig) {
        if (!configType().isInstance(providerConfig)) {
            throw new IllegalArgumentException("Provider config type mismatch for " + providerType());
        }
        return create(configType().cast(providerConfig));
    }
}
