package ai.codegeist.app.tool;

import ai.codegeist.app.session.ToolSessionPart;
import ai.codegeist.app.session.ToolSessionPart.ToolSessionPartStatus;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * Recording wrapper for externally supplied Spring AI callbacks such as MCP tools.
 *
 * <p>Local Codegeist tools already record through {@link CodegeistLocalToolCallback}.
 * MCP callbacks come from Spring AI, so this wrapper preserves their public
 * definition and metadata while applying Codegeist output bounds and session-part
 * recording. Expected callback failures are returned to the model as bounded text
 * and recorded as failed tool parts.
 */
@RequiredArgsConstructor
final class RecordingToolCallback implements ToolCallback {

    private final ToolCallback delegate;

    private final ToolOutputBounds outputBounds;

    private final Consumer<ToolSessionPart> recorder;

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        return callAndRecord(() -> delegate.call(toolInput));
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        return callAndRecord(() -> delegate.call(toolInput, toolContext));
    }

    private String callAndRecord(Supplier<String> invocation) {
        try {
            String outputPreview = outputBounds.preview(invocation.get());
            record(ToolSessionPartStatus.completed, outputPreview);
            return outputPreview;
        }
        catch (RuntimeException exception) {
            String outputPreview = outputBounds.errorPreview(exception.getMessage());
            record(ToolSessionPartStatus.failed, outputPreview);
            return outputPreview;
        }
    }

    private void record(ToolSessionPartStatus status, String outputPreview) {
        ToolSessionPart part = new ToolSessionPart();
        part.setId(UUID.randomUUID());
        part.setTool(delegate.getToolDefinition().name());
        part.setStatus(status);
        part.setOutputPreview(outputPreview);
        recorder.accept(part);
    }
}
