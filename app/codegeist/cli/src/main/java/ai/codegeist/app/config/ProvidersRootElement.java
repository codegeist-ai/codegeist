package ai.codegeist.app.config;

public class ProvidersRootElement extends CodegeistConfigRootElement<ProvidersConfig> {

    public static final String ROOT_NAME = "provider";

    public ProvidersRootElement() {
        this(new ProvidersConfig());
    }

    public ProvidersRootElement(ProvidersConfig config) {
        super(ROOT_NAME, config);
    }
}
