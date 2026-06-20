package ai.codegeist.app.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import ai.codegeist.app.session.ToolSessionPart;
import ai.codegeist.app.session.ToolSessionPart.ToolSessionPartStatus;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

class CodegeistToolServiceTest {

    private static final String TOOL_NAME = "fake_tool";
    private static final String TOOL_OUTPUT = "fake output";

    @TempDir
    private Path tempDir;

    @Test
    void opensRunWithLocalCallbacksAndRecordsToolParts() {
        CodegeistToolService service = new CodegeistToolService(new CodegeistLocalTools(
                new ToolOutputBounds(),
                List.of(new FakeLocalTool())));

        CodegeistToolRun run = service.openRun(tempDir);
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
        CodegeistToolService service = new CodegeistToolService(new CodegeistLocalTools(
                new ToolOutputBounds(),
                List.of(new FakeLocalTool())));

        CodegeistToolRun run = service.openRun(tempDir);
        run.executionContext().toolCallbacks().get(0).call("{}");
        List<ToolSessionPart> completedToolParts = run.completedToolParts();

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> completedToolParts.add(new ToolSessionPart()));
        assertThat(run.completedToolParts()).hasSize(1);
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
}
