package ai.codegeist.app.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Objects;
import lombok.SneakyThrows;

final class ProviderConfigJacksonConverter {

    private ProviderConfigJacksonConverter() {
    }

    @SneakyThrows(JsonProcessingException.class)
    static ProviderConfig convert(ObjectNode node, ObjectCodec codec) {
        JsonNode typeNode = node.get("type");
        if (typeNode == null || !typeNode.isTextual() || typeNode.asText().isBlank()) {
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
        return Arrays.stream(ProviderConfig.class.getPermittedSubclasses())
                .filter(ProviderConfig.class::isAssignableFrom)
                .map(ProviderConfigJacksonConverter::asProviderConfigClass)
                .filter(providerClass -> {
                    Provider provider = providerClass.getAnnotation(Provider.class);
                    return provider != null && Objects.equals(type, provider.value());
                })
                .findFirst()
                .orElseThrow(() -> new CodegeistConfigValidationException("Unsupported provider type: " + type));
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends ProviderConfig> asProviderConfigClass(Class<?> providerClass) {
        return (Class<? extends ProviderConfig>) providerClass;
    }
}
