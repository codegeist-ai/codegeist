package ai.codegeist.app.chat;

import ai.codegeist.app.config.OllamaProviderConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OllamaChatModelFactory implements ProviderChatModelFactory<OllamaProviderConfig> {

    private static final String PROVIDER_TYPE = "ollama";
    private static final String TEMPERATURE_OPTION = "temperature";
    private static final String SEED_OPTION = "seed";

    @Override
    public Class<OllamaProviderConfig> configType() {
        return OllamaProviderConfig.class;
    }

    @Override
    public String providerType() {
        return PROVIDER_TYPE;
    }

    @Override
    public ChatModel create(OllamaProviderConfig providerConfig) {
        log.debug("Creating Ollama chat model for {}", providerConfig.getBaseUrl());
        OllamaApi ollamaApi = OllamaApi.builder()
                .baseUrl(providerConfig.getBaseUrl())
                .build();
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model(providerConfig.getModel())
                .temperature(doubleOption(providerConfig.getOptions(), TEMPERATURE_OPTION))
                .seed(integerOption(providerConfig.getOptions(), SEED_OPTION))
                .build();
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(options)
                .build();
    }

    private Double doubleOption(Map<String, Object> options, String name) {
        Object value = option(options, name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            return Double.valueOf(text);
        }
        throw new IllegalArgumentException("Ollama option must be numeric: " + name);
    }

    private Integer integerOption(Map<String, Object> options, String name) {
        Object value = option(options, name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            return Integer.valueOf(text);
        }
        throw new IllegalArgumentException("Ollama option must be numeric: " + name);
    }

    private Object option(Map<String, Object> options, String name) {
        if (options == null) {
            return null;
        }
        return options.get(name);
    }
}
