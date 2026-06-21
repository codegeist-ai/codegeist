package ai.codegeist.app.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistConfigElement;
import ai.codegeist.app.config.CodegeistConfigRootElement;
import ai.codegeist.app.config.McpClientConfig;
import ai.codegeist.app.config.McpClientsConfig;
import ai.codegeist.app.config.McpClientsRootElement;
import ai.codegeist.app.config.StdioMcpClientConfig;
import ai.codegeist.app.config.StreamableHttpMcpClientConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

class CodegeistMcpAdapterTest {

    private static final String STDIO_CLIENT_ID = "filesystem";
    private static final String REMOTE_CLIENT_ID = "remote";
    private static final String TOOL_NAME = "fake_mcp_tool";
    private static final String TOOL_OUTPUT = "fake mcp output";

    @Test
    void absentMcpConfigReturnsEmptyRun() {
        RecordingMcpClientFactory factory = new RecordingMcpClientFactory();
        CodegeistMcpAdapter adapter = new CodegeistMcpAdapter(factory);

        CodegeistMcpRun run = adapter.openRun(new CodegeistConfig());

        assertThat(run.getToolCallbacks()).isEmpty();
        assertThat(factory.openedClients).isEmpty();
    }

    @Test
    void emptyMcpConfigReturnsEmptyRun() {
        RecordingMcpClientFactory factory = new RecordingMcpClientFactory();
        CodegeistMcpAdapter adapter = new CodegeistMcpAdapter(factory);

        CodegeistMcpRun run = adapter.openRun(configWithMcp(new McpClientsConfig()));

        assertThat(run.getToolCallbacks()).isEmpty();
        assertThat(factory.openedClients).isEmpty();
    }

    @Test
    void stdioConfigMapsIntoClientCreationPathAndExposesCallbacks() {
        RecordingMcpClientFactory factory = new RecordingMcpClientFactory();
        CodegeistMcpAdapter adapter = new CodegeistMcpAdapter(factory);
        StdioMcpClientConfig client = stdioClient(STDIO_CLIENT_ID);
        client.setCommand("npx");

        CodegeistMcpRun run = adapter.openRun(configWithClients(client));

        assertThat(factory.openedClients).containsExactly(client);
        assertThat(run.getToolCallbacks()).singleElement().satisfies(callback -> {
            assertThat(callback.getToolDefinition().name()).isEqualTo(TOOL_NAME);
            assertThat(callback.call("{}")).isEqualTo(TOOL_OUTPUT);
        });
    }

    @Test
    void streamableHttpConfigMapsIntoClientCreationPath() {
        RecordingMcpClientFactory factory = new RecordingMcpClientFactory();
        CodegeistMcpAdapter adapter = new CodegeistMcpAdapter(factory);
        StreamableHttpMcpClientConfig client = streamableHttpClient(REMOTE_CLIENT_ID);
        client.setUrl("http://127.0.0.1:3000");

        CodegeistMcpRun run = adapter.openRun(configWithClients(client));

        assertThat(factory.openedClients).containsExactly(client);
        assertThat(run.getToolCallbacks()).hasSize(1);
    }

    @Test
    void closeClosesCreatedResources() {
        RecordingMcpClientFactory factory = new RecordingMcpClientFactory();
        CodegeistMcpAdapter adapter = new CodegeistMcpAdapter(factory);
        StdioMcpClientConfig client = stdioClient(STDIO_CLIENT_ID);
        client.setCommand("npx");

        CodegeistMcpRun run = adapter.openRun(configWithClients(client));
        run.close();

        assertThat(factory.closeables).singleElement().satisfies(closeable -> assertThat(closeable.closed).isTrue());
    }

    private StubConfig configWithClients(McpClientConfig... clients) {
        McpClientsConfig config = new McpClientsConfig();
        config.getElements().addAll(List.of(clients));
        return configWithMcp(config);
    }

    private StubConfig configWithMcp(McpClientsConfig config) {
        return new StubConfig(new McpClientsRootElement(config));
    }

    private StdioMcpClientConfig stdioClient(String id) {
        StdioMcpClientConfig client = new StdioMcpClientConfig();
        client.setId(id);
        return client;
    }

    private StreamableHttpMcpClientConfig streamableHttpClient(String id) {
        StreamableHttpMcpClientConfig client = new StreamableHttpMcpClientConfig();
        client.setId(id);
        return client;
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

    private static final class RecordingMcpClientFactory implements CodegeistMcpClientFactory {

        private final List<McpClientConfig> openedClients = new ArrayList<>();
        private final List<RecordingCloseable> closeables = new ArrayList<>();

        @Override
        public CodegeistMcpClientHandle openClient(McpClientConfig clientConfig) {
            openedClients.add(clientConfig);
            RecordingCloseable closeable = new RecordingCloseable();
            closeables.add(closeable);
            return new CodegeistMcpClientHandle(List.of(new FakeToolCallback()), closeable);
        }
    }

    private static final class RecordingCloseable implements AutoCloseable {

        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }
    }

    private static final class FakeToolCallback implements ToolCallback {

        @Override
        public ToolDefinition getToolDefinition() {
            return ToolDefinition.builder()
                    .name(TOOL_NAME)
                    .description("Fake MCP tool")
                    .inputSchema("{\"type\":\"object\"}")
                    .build();
        }

        @Override
        public String call(String toolInput) {
            return TOOL_OUTPUT;
        }
    }
}
