package ai.codegeist.app.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class McpClientsRootElement extends CodegeistConfigRootElement<McpClientConfig> {

    public static final String ROOT_NAME = "mcp";
    static final String ROOT_OBJECT_MESSAGE = "mcp root element must be a YAML object";

    @Valid
    private Map<@NotBlank String, @Valid McpClientConfig> clients = new LinkedHashMap<>();

    @Override
    public McpClientsRootElement parse(JsonNode source, CodegeistConfigYamlMapper yamlMapper) {
        McpClientsRootElement root = new McpClientsRootElement();
        root.clients = convertClients(source, yamlMapper);
        return root;
    }

    @Override
    public Object toYamlValue() {
        return clients;
    }

    @Override
    public String rootName() {
        return ROOT_NAME;
    }

    private Map<String, McpClientConfig> convertClients(JsonNode source, CodegeistConfigYamlMapper yamlMapper) {
        if (source == null || source.isNull()) {
            return new LinkedHashMap<>();
        }
        if (!(source instanceof ObjectNode)) {
            throw new CodegeistConfigValidationException(ROOT_OBJECT_MESSAGE);
        }
        return yamlMapper.convertValue(source, new TypeReference<LinkedHashMap<String, McpClientConfig>>() {
        });
    }
}
