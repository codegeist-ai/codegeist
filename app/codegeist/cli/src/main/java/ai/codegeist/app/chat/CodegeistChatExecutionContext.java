package ai.codegeist.app.chat;

import java.nio.file.Path;
import java.util.List;
import lombok.NonNull;
import org.springframework.ai.tool.ToolCallback;

/**
 * Runtime-only context for one provider chat call.
 *
 * <p>The context carries prompt-scoped tool callbacks beside
 * {@link CodegeistChatRequest} so the request record remains limited to model and
 * prompt. It is not serialized to the session store and must not grow provider,
 * model, MCP config, or session-state fields.
 */
public record CodegeistChatExecutionContext(
        @NonNull Path workingDirectory,
        @NonNull List<ToolCallback> toolCallbacks) {

    public CodegeistChatExecutionContext {
        toolCallbacks = List.copyOf(toolCallbacks);
    }

    public static CodegeistChatExecutionContext empty(@NonNull Path workingDirectory) {
        return new CodegeistChatExecutionContext(workingDirectory, List.of());
    }

    public ToolCallback[] toolCallbackArray() {
        return toolCallbacks.toArray(ToolCallback[]::new);
    }
}
