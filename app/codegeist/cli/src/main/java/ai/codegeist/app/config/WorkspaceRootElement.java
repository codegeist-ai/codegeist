package ai.codegeist.app.config;

/** Top-level {@code workspace:} root model for direct Codegeist config. */
public class WorkspaceRootElement extends CodegeistConfigRootElement<WorkspaceConfig> {

    public static final String ROOT_NAME = "workspace";

    public WorkspaceRootElement() {
        this(new WorkspaceConfig());
    }

    public WorkspaceRootElement(WorkspaceConfig config) {
        super(ROOT_NAME, config);
    }
}
