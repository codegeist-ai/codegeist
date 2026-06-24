package ai.codegeist.app.tool;

import ai.codegeist.app.session.ToolSessionPart;
import ai.codegeist.app.session.ToolSessionPart.ToolSessionPartStatus;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * Spring AI callback wrapper for Codegeist-owned local tools.
 *
 * <p>The callback boundary is where local tool output becomes model-visible and
 * session-store-visible. Completed local tool output is bounded at this edge
 * before it is returned or recorded. Every handled tool failure is converted to a
 * bounded string and recorded as a failed {@link ToolSessionPart}; unexpected
 * programming errors are allowed to escape so tests can expose them.
 */
@RequiredArgsConstructor
final class CodegeistLocalToolCallback implements ToolCallback {

    private final ToolDefinition definition;

    private final ToolMetadata metadata;

    private final Function<CodegeistToolInput, CodegeistToolResult> executor;

    private final ToolOutputBounds outputBounds;

    private final Consumer<ToolSessionPart> recorder;

    @Override
    public ToolDefinition getToolDefinition() {
        return definition;
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return metadata;
    }

    @Override
    public String call(String toolInput) {
        try {
            String outputPreview = outputBounds.preview(
                    executor.apply(new CodegeistToolInput(toolInput)).outputPreview());
            record(ToolSessionPartStatus.completed, outputPreview);
            return outputPreview;
        }
        catch (CodegeistToolException exception) {
            String outputPreview = outputBounds.errorPreview(exception.getMessage());
            record(ToolSessionPartStatus.failed, outputPreview);
            return outputPreview;
        }
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        return call(toolInput);
    }

    private void record(ToolSessionPartStatus status, String outputPreview) {
        ToolSessionPart part = new ToolSessionPart();
        part.setId(UUID.randomUUID());
        part.setTool(definition.name());
        part.setStatus(status);
        part.setOutputPreview(outputPreview);
        recorder.accept(part);
    }

}
