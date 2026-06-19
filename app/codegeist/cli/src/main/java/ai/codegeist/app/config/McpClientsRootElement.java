package ai.codegeist.app.config;

/**
 * Top-level {@code mcp:} root model for direct Codegeist config.
 *
 * <p>The root stores a {@link McpClientsConfig} element. That config owns the
 * ordered client list and renders the familiar provider-style YAML object where the
 * entry key is the client id.
 */
public class McpClientsRootElement extends CodegeistConfigRootElement<McpClientsConfig> {

    public static final String ROOT_NAME = "mcp";

    public McpClientsRootElement() {
        this(new McpClientsConfig());
    }

    public McpClientsRootElement(McpClientsConfig config) {
        super(ROOT_NAME, config);
    }
}
