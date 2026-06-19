package ai.codegeist.app.config;

public class McpClientsConfig extends CodegeistConfigKeyedListElement<McpClientConfig> {

    @Override
    protected String key(McpClientConfig client) {
        return client.getId();
    }
}
