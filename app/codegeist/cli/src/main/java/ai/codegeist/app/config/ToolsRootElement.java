package ai.codegeist.app.config;

/** Top-level {@code tools:} root model for direct Codegeist config. */
public class ToolsRootElement extends CodegeistConfigRootElement<ToolsConfig> {

    public static final String ROOT_NAME = "tools";

    public ToolsRootElement() {
        this(new ToolsConfig());
    }

    public ToolsRootElement(ToolsConfig config) {
        super(ROOT_NAME, config);
    }
}
