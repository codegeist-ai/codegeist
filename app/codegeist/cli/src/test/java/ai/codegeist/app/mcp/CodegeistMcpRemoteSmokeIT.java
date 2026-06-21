package ai.codegeist.app.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistConfigElement;
import ai.codegeist.app.config.CodegeistConfigRootElement;
import ai.codegeist.app.config.McpClientsConfig;
import ai.codegeist.app.config.McpClientsRootElement;
import ai.codegeist.app.config.StreamableHttpMcpClientConfig;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.StringUtils;

class CodegeistMcpRemoteSmokeIT {

    private static final String REMOTE_SMOKE_URL_PROPERTY = "codegeist.mcp.remote-smoke.url";
    private static final String REMOTE_TOOL_NAME = "remote_echo";

    @Test
    void opensRemoteStreamableHttpCallback() {
        String baseUrl = System.getProperty(REMOTE_SMOKE_URL_PROPERTY);
        assertThat(StringUtils.hasText(baseUrl))
                .as("%s must be set by task mcp-remote-smoke", REMOTE_SMOKE_URL_PROPERTY)
                .isTrue();
        CodegeistMcpAdapter adapter = new CodegeistMcpAdapter(new SpringAiMcpClientFactory());

        try (CodegeistMcpRun run = adapter.openRun(config(baseUrl))) {
            ToolCallback callback = run.getToolCallbacks().stream()
                    .filter(toolCallback -> toolCallback.getToolDefinition().name().contains(REMOTE_TOOL_NAME))
                    .findFirst()
                    .orElseThrow();

            String output = callback.call("{\"text\":\"hello\"}");

            assertThat(output).contains("remote: hello");
        }
    }

    private CodegeistConfig config(String baseUrl) {
        StreamableHttpMcpClientConfig client = new StreamableHttpMcpClientConfig();
        client.setId("remote-smoke");
        client.setUrl(baseUrl);
        McpClientsConfig config = new McpClientsConfig();
        config.getElements().add(client);
        return new StubConfig(new McpClientsRootElement(config));
    }

    private static final class StubConfig extends CodegeistConfig {

        private final CodegeistConfigRootElement<? extends CodegeistConfigElement> rootElement;

        private StubConfig(CodegeistConfigRootElement<? extends CodegeistConfigElement> rootElement) {
            this.rootElement = rootElement;
        }

        @Override
        public <T extends CodegeistConfigRootElement<? extends CodegeistConfigElement>> Optional<T> rootElement(
                Class<T> rootElementType) {
            if (rootElementType.isInstance(rootElement)) {
                return Optional.of(rootElementType.cast(rootElement));
            }
            return Optional.empty();
        }
    }
}
