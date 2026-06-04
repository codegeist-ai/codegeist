package ai.codegeist.app.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

final class ProviderConfigJacksonConverter {

    private static final List<Class<? extends ProviderConfig>> PROVIDER_CLASSES = List.of(
            OllamaProviderConfig.class,
            OpenAiProviderConfig.class);

    private ProviderConfigJacksonConverter() {
    }

    @SneakyThrows(JsonProcessingException.class)
    static ProviderConfig convert(ObjectNode node, ObjectCodec codec) {
        JsonNode typeNode = node.get("type");
        if (typeNode == null || !typeNode.isTextual() || !StringUtils.hasText(typeNode.asText())) {
            throw new CodegeistConfigValidationException("Provider config type is required");
        }

        String providerType = typeNode.asText();
        Class<? extends ProviderConfig> providerClass = findProviderClass(providerType);
        return codec.treeToValue(node, providerClass);
    }

    static ProviderConfig convertValue(Object source, ObjectMapper mapper) {
        if (source == null) {
            return null;
        }

        if (source instanceof ProviderConfig providerConfig) {
            return providerConfig;
        }

        JsonNode node = mapper.valueToTree(source);
        if (!(node instanceof ObjectNode objectNode)) {
            throw new CodegeistConfigValidationException("Provider config entry must be a YAML object");
        }

        return convert(objectNode, mapper);
    }

    private static Class<? extends ProviderConfig> findProviderClass(String type) {
        return PROVIDER_CLASSES
                .stream()
                .filter(providerClass -> {
                    Provider provider = providerClass.getAnnotation(Provider.class);
                    return provider != null && type.equals(provider.value());
                })
                .findFirst()
                .orElseThrow(() -> new CodegeistConfigValidationException("Unsupported provider type: " + type));
    }
}
