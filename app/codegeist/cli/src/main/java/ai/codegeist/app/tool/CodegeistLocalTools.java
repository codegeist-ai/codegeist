package ai.codegeist.app.tool;

import ai.codegeist.app.session.ToolSessionPart;
import java.util.List;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.stereotype.Component;

/**
 * Assembles Codegeist-owned local tool callbacks for the active workspace.
 *
 * <p>The component owns Spring wiring and callback assembly. Concrete local tools
 * are injected as {@link CodegeistLocalTool} components, so adding another local
 * tool does not require a new field, switch branch, or callback construction branch
 * here.
 */
@Component
@RequiredArgsConstructor
public class CodegeistLocalTools {

    private final ToolOutputBounds outputBounds;

    private final List<CodegeistLocalTool> localTools;

    public List<ToolCallback> callbacks(@NonNull Consumer<ToolSessionPart> recorder) {
        ToolMetadata metadata = ToolMetadata.builder().returnDirect(false).build();
        return localTools.stream()
                .map(tool -> callback(tool, metadata, recorder))
                .toList();
    }

    private ToolCallback callback(
            CodegeistLocalTool tool,
            ToolMetadata metadata,
            Consumer<ToolSessionPart> recorder) {
        return new CodegeistLocalToolCallback(
                tool.definition(),
                metadata,
                tool::execute,
                outputBounds,
                recorder);
    }
}
