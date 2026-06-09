package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

public abstract class CodegeistConfigRootElement<T extends CodegeistConfigElement> {

    private static final String TYPE_FIELD = "type";

    public abstract CodegeistConfigRootElement<T> parse(JsonNode source, ObjectMapper objectMapper);

    public abstract Object toYamlValue();

    @JsonIgnore
    public abstract String rootName();

    protected List<T> convertElements(JsonNode source, ObjectMapper objectMapper) {
        List<T> converted = new ArrayList<>();
        if (source == null || source.isNull()) {
            return converted;
        }
        if (!(source instanceof ObjectNode objectNode)) {
            throw new CodegeistConfigValidationException(rootObjectMessage());
        }

        for (Map.Entry<String, JsonNode> element : objectNode.properties()) {
            if (!StringUtils.hasText(element.getKey())) {
                throw new CodegeistConfigValidationException(elementIdRequiredMessage());
            }
            converted.add(convertElement(element.getValue(), objectMapper));
        }
        return converted;
    }

    protected Map<String, Class<? extends T>> elementClasses() {
        return Map.of();
    }

    protected String rootObjectMessage() {
        return rootName() + " root element must be a YAML object";
    }

    protected String elementIdRequiredMessage() {
        return rootName() + " entry id must not be blank";
    }

    protected String elementObjectMessage() {
        return rootName() + " entry must be a YAML object";
    }

    protected String elementTypeRequiredMessage() {
        return rootName() + " entry type is required";
    }

    protected String unsupportedElementTypePrefix() {
        return "Unsupported " + rootName() + " entry type: ";
    }

    private T convertElement(JsonNode configElement, ObjectMapper objectMapper) {
        if (configElement == null || configElement.isNull()) {
            return null;
        }
        if (!(configElement instanceof ObjectNode objectNode)) {
            throw new CodegeistConfigValidationException(elementObjectMessage());
        }
        return convertConfigElement(objectNode, objectMapper);
    }

    @SneakyThrows(JsonProcessingException.class)
    private T convertConfigElement(ObjectNode node, ObjectMapper objectMapper) {
        JsonNode typeNode = node.get(TYPE_FIELD);
        if (typeNode == null || !typeNode.isTextual() || !StringUtils.hasText(typeNode.asText())) {
            throw new CodegeistConfigValidationException(elementTypeRequiredMessage());
        }

        String elementType = typeNode.asText();
        return objectMapper.treeToValue(node, findElementClass(elementType));
    }

    private Class<? extends T> findElementClass(String type) {
        Class<? extends T> elementClass = elementClasses().get(type);
        if (elementClass == null) {
            throw new CodegeistConfigValidationException(unsupportedElementTypePrefix() + type);
        }
        return elementClass;
    }
}
