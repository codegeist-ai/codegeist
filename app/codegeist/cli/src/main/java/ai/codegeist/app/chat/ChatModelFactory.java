package ai.codegeist.app.chat;

import ai.codegeist.app.config.ProviderConfig;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class ChatModelFactory {

    @Autowired
    private List<ProviderChatModelFactory<? extends ProviderConfig>> providerFactories;

    public ChatModel create(ProviderConfig providerConfig) {
        Assert.notNull(providerConfig, "Provider config must not be null");
        Assert.hasText(providerConfig.getType(), "Provider config type must not be blank");
        return providerFactories.stream()
                .filter(factory -> Objects.equals(factory.providerType(), providerConfig.getType()))
                .findFirst()
                .map(factory -> {
                    log.debug("Creating chat model for provider type {}", providerConfig.getType());
                    return factory.createFrom(providerConfig);
                })
                .orElseThrow(() -> new IllegalArgumentException("Unsupported provider type: " + providerConfig.getType()));
    }
}
