package ai.codegeist.app.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistConfigElement;
import ai.codegeist.app.config.CodegeistConfigRootElement;
import ai.codegeist.app.config.McpClientsConfig;
import ai.codegeist.app.config.McpClientsRootElement;
import ai.codegeist.app.config.StdioMcpClientConfig;
import ai.codegeist.app.mcp.CodegeistMcpAdapter;
import ai.codegeist.app.mcp.CodegeistMcpRun;
import ai.codegeist.app.mcp.TestMcpAdapters;
import ai.codegeist.app.session.ToolSessionPart;
import ai.codegeist.app.session.ToolSessionPart.ToolSessionPartStatus;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

class CodegeistToolServiceTest {

    private static final String TOOL_NAME = "fake_tool";
    private static final String TOOL_OUTPUT = "fake output";
    private static final String MCP_TOOL_NAME = "fake_mcp_tool";
    private static final String MCP_TOOL_OUTPUT = "fake mcp output";

    @TempDir
    private Path tempDir;

    @Test
    void opensRunWithLocalCallbacksAndRecordsToolParts() {
        CodegeistToolService service = service(TestMcpAdapters.empty());

        CodegeistToolRun run = service.openRun(new CodegeistConfig(), tempDir);
        assertThat(run.executionContext().workingDirectory()).isEqualTo(tempDir);
        ToolCallback callback = run.executionContext().toolCallbacks().get(0);

        String output = callback.call("{}");

        assertThat(output).isEqualTo(TOOL_OUTPUT);
        assertThat(run.completedToolParts()).singleElement().satisfies(part -> {
            assertThat(part.getTool()).isEqualTo(TOOL_NAME);
            assertThat(part.getStatus()).isEqualTo(ToolSessionPartStatus.completed);
            assertThat(part.getOutputPreview()).isEqualTo(TOOL_OUTPUT);
        });
    }

    @Test
    void completedToolPartsAreDefensiveCopies() {
        CodegeistToolService service = service(TestMcpAdapters.empty());

        CodegeistToolRun run = service.openRun(new CodegeistConfig(), tempDir);
        run.executionContext().toolCallbacks().get(0).call("{}");
        List<ToolSessionPart> completedToolParts = run.completedToolParts();

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> completedToolParts.add(new ToolSessionPart()));
        assertThat(run.completedToolParts()).hasSize(1);
    }

    @Test
    void includesConfiguredMcpCallbacksAndRecordsBoundedResults() {
        CodegeistToolService service = service(TestMcpAdapters.withRun(TestMcpAdapters.runWithCallbacks(
                List.of(new FakeMcpToolCallback()))));

        CodegeistToolRun run = service.openRun(configWithMcpClient(), tempDir);
        ToolCallback mcpCallback = run.executionContext().toolCallbacks().stream()
                .filter(callback -> callback.getToolDefinition().name().equals(MCP_TOOL_NAME))
                .findFirst()
                .orElseThrow();

        String output = mcpCallback.call("{}");

        assertThat(output).isEqualTo(MCP_TOOL_OUTPUT);
        assertThat(run.completedToolParts()).singleElement().satisfies(part -> {
            assertThat(part.getTool()).isEqualTo(MCP_TOOL_NAME);
            assertThat(part.getStatus()).isEqualTo(ToolSessionPartStatus.completed);
            assertThat(part.getOutputPreview()).isEqualTo(MCP_TOOL_OUTPUT);
        });
    }

    @Test
    void recordsMcpFailuresAsBoundedFailedToolParts() {
        CodegeistToolService service = service(TestMcpAdapters.withRun(TestMcpAdapters.runWithCallbacks(
                List.of(new FailingMcpToolCallback()))));

        CodegeistToolRun run = service.openRun(configWithMcpClient(), tempDir);
        ToolCallback mcpCallback = run.executionContext().toolCallbacks().stream()
                .filter(callback -> callback.getToolDefinition().name().equals(MCP_TOOL_NAME))
                .findFirst()
                .orElseThrow();

        String output = mcpCallback.call("{}");

        assertThat(output).contains("remote failure");
        assertThat(run.completedToolParts()).singleElement().satisfies(part -> {
            assertThat(part.getTool()).isEqualTo(MCP_TOOL_NAME);
            assertThat(part.getStatus()).isEqualTo(ToolSessionPartStatus.failed);
            assertThat(part.getOutputPreview()).isEqualTo(output);
        });
    }

    @Test
    void closesMcpRunWhenToolRunCloses() {
        RecordingMcpRun mcpRun = new RecordingMcpRun(List.of(new FakeMcpToolCallback()));
        CodegeistToolService service = service(TestMcpAdapters.withRun(mcpRun));

        CodegeistToolRun run = service.openRun(configWithMcpClient(), tempDir);
        run.close();

        assertThat(mcpRun.closed).isTrue();
    }

    private CodegeistToolService service(CodegeistMcpAdapter mcpAdapter) {
        ToolOutputBounds bounds = new ToolOutputBounds();
        return new CodegeistToolService(new CodegeistLocalTools(
                bounds,
                List.of(new FakeLocalTool())),
                bounds,
                mcpAdapter);
    }

    private CodegeistConfig configWithMcpClient() {
        StdioMcpClientConfig client = new StdioMcpClientConfig();
        client.setId("fake");
        client.setCommand("fake-command");
        McpClientsConfig mcpConfig = new McpClientsConfig();
        mcpConfig.getElements().add(client);
        return new StubConfig(new McpClientsRootElement(mcpConfig));
    }

    private static final class FakeLocalTool implements CodegeistLocalTool {

        @Override
        public ToolDefinition definition() {
            return ToolDefinition.builder()
                    .name(TOOL_NAME)
                    .description("Fake local tool")
                    .inputSchema("{\"type\":\"object\"}")
                    .build();
        }

        @Override
        public CodegeistToolResult execute(CodegeistToolInput toolInput) {
            return new CodegeistToolResult(TOOL_OUTPUT);
        }
    }

    private static class FakeMcpToolCallback implements ToolCallback {

        @Override
        public ToolDefinition getToolDefinition() {
            return ToolDefinition.builder()
                    .name(MCP_TOOL_NAME)
                    .description("Fake MCP tool")
                    .inputSchema("{\"type\":\"object\"}")
                    .build();
        }

        @Override
        public ToolMetadata getToolMetadata() {
            return ToolMetadata.builder().returnDirect(false).build();
        }

        @Override
        public String call(String toolInput) {
            return MCP_TOOL_OUTPUT;
        }

        @Override
        public String call(String toolInput, ToolContext toolContext) {
            return call(toolInput);
        }
    }

    private static final class FailingMcpToolCallback extends FakeMcpToolCallback {

        @Override
        public String call(String toolInput) {
            throw new IllegalStateException("remote failure for test");
        }
    }

    private static final class RecordingMcpRun implements CodegeistMcpRun {

        private final List<ToolCallback> callbacks;
        private boolean closed;

        private RecordingMcpRun(List<ToolCallback> callbacks) {
            this.callbacks = callbacks;
        }

        @Override
        public List<ToolCallback> getToolCallbacks() {
            return callbacks;
        }

        @Override
        public void close() {
            closed = true;
        }
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
