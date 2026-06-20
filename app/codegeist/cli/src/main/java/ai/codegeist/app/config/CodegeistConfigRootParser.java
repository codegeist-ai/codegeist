package ai.codegeist.app.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Central parser for the direct top-level roots in {@code codegeist.yml}.
 *
 * <p>Root element classes are deliberately plain models, not Spring beans. This
 * service owns the framework boundary: it validates each supported root shape,
 * converts Jackson nodes with the shared {@link CodegeistConfigYamlMapper}, and
 * returns root model instances for {@link CodegeistConfigService} to validate and
 * store.
 *
 * <p>The parser keeps the user-facing YAML shape separate from the runtime model:
 * {@code provider:} and {@code mcp:} are both provider-style YAML objects keyed by
 * user-chosen ids, while their runtime payloads are list-backed
 * {@link CodegeistConfigKeyedListElement} instances. {@code workspace:} is a single
 * object root and therefore maps directly into a {@link WorkspaceConfig}. Provider
 * entries additionally dispatch on their inner {@code type} field because the YAML
 * key is only an id, not the implementation selector. MCP entries use the YAML key as
 * {@link McpClientConfig#getId()} and keep {@code type} as the transport kind.
 *
 * <p>Adding a new top-level direct YAML root should start here: choose whether the
 * root is a single object, a keyed list with key-copy semantics, or a typed keyed
 * list with explicit class dispatch. Keep root model classes framework-free and put
 * Jackson-specific conversion in this parser or {@link CodegeistConfigYamlMapper}.
 */
@Service
@RequiredArgsConstructor
public class CodegeistConfigRootParser {

    static final String UNSUPPORTED_ROOT_ELEMENT_PREFIX = "Unsupported Codegeist config root element: ";
    static final String WORKSPACE_MAPPING_ERROR_PREFIX = "Invalid workspace root element";

    private static final String TYPE_FIELD = "type";
    // Keep provider dispatch explicit so native images do not need runtime scanning.
    private static final Map<String, Class<? extends ProviderConfig>> PROVIDER_CLASSES = Map.of(
            OllamaProviderConfig.PROVIDER_TYPE, OllamaProviderConfig.class,
            OpenAiProviderConfig.PROVIDER_TYPE, OpenAiProviderConfig.class);

    private final CodegeistConfigYamlMapper yamlMapper;

    /**
     * Parses one top-level field from direct {@code codegeist.yml} into its root model.
     *
     * <p>{@link CodegeistConfigService} has already verified that the file root is a
     * YAML object and calls this method once per top-level field. Unsupported names are
     * rejected here instead of being ignored, so misspelled roots fail close to the
     * input that caused them. The returned root still needs Bean Validation, which is
     * owned by {@link CodegeistConfigService#loadConfig(String)} after all roots are
     * collected into one {@link CodegeistConfig}.
     */
    public CodegeistConfigRootElement<? extends CodegeistConfigElement> parseRootElement(@NonNull String rootName,
            @NonNull JsonNode source) {
        return switch (rootName) {
            case ProvidersRootElement.ROOT_NAME -> parseProviders(source);
            case McpClientsRootElement.ROOT_NAME -> parseMcpClients(source);
            case WorkspaceRootElement.ROOT_NAME -> parseWorkspace(source);
            default -> throw new CodegeistConfigValidationException(UNSUPPORTED_ROOT_ELEMENT_PREFIX + rootName);
        };
    }

    /**
     * Parses the {@code provider:} root as a keyed YAML object backed by a list.
     *
     * <p>The provider YAML key is validated as an id, but concrete Java class dispatch
     * is based only on the nested {@code type} field. This preserves the current
     * contract where ids can be arbitrary labels while provider implementation classes
     * come from the explicit registry below.
     */
    private ProvidersRootElement parseProviders(JsonNode source) {
        ProvidersConfig config = new ProvidersConfig();
        for (Map.Entry<String, JsonNode> entry : rootEntries(source, ProvidersRootElement.ROOT_NAME)) {
            requireEntryId(ProvidersRootElement.ROOT_NAME, entry.getKey());
            config.getElements().add(parseProvider(entry.getValue()));
        }
        return new ProvidersRootElement(config);
    }

    /**
     * Parses the {@code mcp:} root as a keyed YAML object backed by a list.
     *
     * <p>The MCP YAML key is client identity, so the parsed client receives it through
     * {@link McpClientConfig#setId(String)} after Jackson maps the entry body. The
     * inner {@code type} field remains a transport value and is not used for Java class
     * dispatch in this slice.
     */
    private McpClientsRootElement parseMcpClients(JsonNode source) {
        McpClientsConfig config = new McpClientsConfig();
        for (Map.Entry<String, JsonNode> entry : rootEntries(source, McpClientsRootElement.ROOT_NAME)) {
            requireEntryId(McpClientsRootElement.ROOT_NAME, entry.getKey());
            config.getElements().add(parseMcpClient(entry.getKey(), entry.getValue()));
        }
        return new McpClientsRootElement(config);
    }

    /**
     * Parses the single-object {@code workspace:} root.
     *
     * <p>This path does not look for nested ids or {@code type} dispatch. Non-object
     * values fail before Jackson conversion so users get a concise root-shape error
     * instead of a generic mapping exception.
     */
    private WorkspaceRootElement parseWorkspace(JsonNode source) {
        ObjectNode workspaceObject = requireRootObject(source, WorkspaceRootElement.ROOT_NAME);
        try {
            return new WorkspaceRootElement(yamlMapper.convertValue(workspaceObject, WorkspaceConfig.class));
        }
        catch (IllegalArgumentException exception) {
            throw new CodegeistConfigValidationException(WORKSPACE_MAPPING_ERROR_PREFIX, exception);
        }
    }

    /**
     * Returns the keyed entries for a provider-style root while preserving declaration
     * order.
     *
     * <p>A present {@code null} root means an empty list. Non-null values must be YAML
     * objects because both {@code provider:} and {@code mcp:} expose object-shaped
     * public config even though their runtime payloads are list-backed.
     */
    private Iterable<Map.Entry<String, JsonNode>> rootEntries(JsonNode source, String rootName) {
        if (source == null || source.isNull()) {
            return List.of();
        }
        return requireRootObject(source, rootName).properties();
    }

    /**
     * Requires one provider-style entry id to be non-blank.
     */
    private void requireEntryId(String rootName, String id) {
        if (!StringUtils.hasText(id)) {
            throw new CodegeistConfigValidationException(rootName + " entry id must not be blank");
        }
    }

    /**
     * Parses one provider entry after its surrounding YAML key has been validated.
     *
     * <p>A {@code null} entry is preserved as {@code null}. Non-null entries must be a
     * YAML object so {@code type} can be inspected before Jackson maps to a concrete
     * provider config class. The validated {@code type} is copied into the shared base
     * field for Bean Validation, while concrete providers still expose constant
     * read-only output types.
     */
    private ProviderConfig parseProvider(JsonNode source) {
        if (source == null || source.isNull()) {
            return null;
        }

        ObjectNode objectNode = requireEntryObject(source, ProvidersRootElement.ROOT_NAME);
        String type = requiredType(ProvidersRootElement.ROOT_NAME, objectNode);
        ProviderConfig provider = yamlMapper.convertValue(objectNode, providerClass(type));
        provider.setType(type);
        return provider;
    }

    /**
     * Parses one MCP client entry and copies the YAML key into {@link McpClientConfig}.
     */
    private McpClientConfig parseMcpClient(String id, JsonNode source) {
        if (source == null || source.isNull()) {
            return null;
        }

        McpClientConfig client = yamlMapper.convertValue(
                requireEntryObject(source, McpClientsRootElement.ROOT_NAME), McpClientConfig.class);
        client.setId(id);
        return client;
    }

    /**
     * Reads and validates the dispatch {@code type} from one typed entry object.
     *
     * <p>Provider keys such as {@code openai:} and {@code local:} are ids only; they
     * never select Java classes. Requiring a textual, non-blank {@code type} here keeps
     * that contract explicit and avoids falling back to map keys when users omit or
     * misspell the dispatch field.
     */
    private String requiredType(String rootName, ObjectNode source) {
        JsonNode typeNode = source.get(TYPE_FIELD);
        if (typeNode == null || !typeNode.isTextual() || !StringUtils.hasText(typeNode.asText())) {
            throw new CodegeistConfigValidationException(rootName + " entry type is required");
        }
        return typeNode.asText();
    }

    /**
     * Requires a root-level YAML object and reports the root name in the error.
     */
    private ObjectNode requireRootObject(JsonNode source, String rootName) {
        if (source instanceof ObjectNode objectNode) {
            return objectNode;
        }
        throw new CodegeistConfigValidationException(rootObjectMessage(rootName));
    }

    /** Returns the stable root-shape error string used by direct config tests. */
    static String rootObjectMessage(String rootName) {
        return rootName + " root element must be a YAML object";
    }

    /** Requires one keyed entry body to be a YAML object. */
    private ObjectNode requireEntryObject(JsonNode source, String rootName) {
        if (source instanceof ObjectNode objectNode) {
            return objectNode;
        }
        throw new CodegeistConfigValidationException(rootName + " entry must be a YAML object");
    }

    /**
     * Looks up the concrete typed config class for an already validated type value.
     *
     * <p>The registry is intentionally explicit instead of classpath-scanned so native
     * images do not need broad reflection or resource metadata for provider discovery.
     */
    private Class<? extends ProviderConfig> providerClass(String type) {
        Class<? extends ProviderConfig> providerClass = PROVIDER_CLASSES.get(type);
        if (providerClass == null) {
            throw new CodegeistConfigValidationException(
                    "Unsupported " + ProvidersRootElement.ROOT_NAME + " entry type: " + type);
        }
        return providerClass;
    }
}
