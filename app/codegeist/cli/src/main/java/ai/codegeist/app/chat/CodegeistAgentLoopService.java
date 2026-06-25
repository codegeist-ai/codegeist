package ai.codegeist.app.chat;

import ai.codegeist.app.config.ProviderConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

/**
 * Synchronous Codegeist-owned model/tool/model control loop for one chat request.
 *
 * <p>The loop receives a provider config, public chat request, and already-open
 * prompt-scoped execution context from {@link ChatHarnessService}. The harness owns
 * provider selection, tool-run opening/closing, and session persistence. This service
 * owns only the in-memory provider history for the active request: it submits a
 * {@link UserMessage}, inspects assistant tool calls in the raw Spring AI response,
 * dispatches matching {@link ToolCallback} instances, appends a
 * {@link ToolResponseMessage}, and calls the model again until final assistant text
 * arrives. Tool callbacks keep their existing responsibility for output bounding and
 * {@code ToolSessionPart} recording.
 *
 * <p>Failure policy is intentionally small for the first loop slice: duplicate tool
 * names fail before side effects, missing tools become model-visible tool results,
 * callback wrappers convert handled local/MCP failures into bounded text, unexpected
 * programming errors may escape, and {@link #MAX_TOOL_ROUNDS} prevents unbounded
 * repeated tool calls.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodegeistAgentLoopService {

    static final int MAX_TOOL_ROUNDS = 8;
    static final String MAX_TOOL_ROUNDS_MESSAGE = "Agent tool loop exceeded 8 rounds";
    static final String UNKNOWN_TOOL_MESSAGE_PREFIX = "Unknown tool requested: ";
    static final String DUPLICATE_TOOL_MESSAGE_PREFIX = "Duplicate tool callback name: ";
    static final String EMPTY_TOOL_ARGUMENTS = "{}";

    private final CodegeistChatService chatService;

    /**
     * Runs one synchronous prompt request until the selected model returns final text.
     *
     * @param providerConfig selected provider access config; not stored in session
     * @param request public prompt request containing only runtime model and user text
     * @param context prompt-scoped working directory and tool callbacks
     * @return final assistant response text after any tool continuations
     * @throws IllegalStateException when duplicate callback names make dispatch
     * ambiguous or when the max tool-round guard is exceeded
     */
    public CodegeistChatResponse run(
            @NonNull ProviderConfig providerConfig,
            @NonNull CodegeistChatRequest request,
            @NonNull CodegeistChatExecutionContext context) {
        Map<String, ToolCallback> callbacksByName = callbacksByName(context.toolCallbacks());
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(request.prompt()));
        int toolRounds = 0;

        while (true) {
            // The loop mutates `messages` only between synchronous provider calls.
            ChatResponse chatResponse = chatService.rawChat(
                    providerConfig,
                    new CodegeistChatTurnRequest(request.model(), messages),
                    context);
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            if (!assistantMessage.hasToolCalls()) {
                log.debug("Agent loop completed after {} tool rounds", toolRounds);
                return new CodegeistChatResponse(assistantMessage.getText());
            }

            if (toolRounds >= MAX_TOOL_ROUNDS) {
                throw new IllegalStateException(MAX_TOOL_ROUNDS_MESSAGE);
            }

            messages.add(assistantMessage);
            messages.add(toolResponseMessage(assistantMessage.getToolCalls(), callbacksByName));
            toolRounds++;
        }
    }

    private Map<String, ToolCallback> callbacksByName(List<ToolCallback> callbacks) {
        Map<String, ToolCallback> callbacksByName = new LinkedHashMap<>();
        for (ToolCallback callback : callbacks) {
            String toolName = callback.getToolDefinition().name();
            ToolCallback existing = callbacksByName.putIfAbsent(toolName, callback);
            if (existing != null) {
                throw new IllegalStateException(DUPLICATE_TOOL_MESSAGE_PREFIX + toolName);
            }
        }
        return callbacksByName;
    }

    private ToolResponseMessage toolResponseMessage(
            List<AssistantMessage.ToolCall> toolCalls,
            Map<String, ToolCallback> callbacksByName) {
        List<ToolResponseMessage.ToolResponse> responses = new ArrayList<>();
        for (AssistantMessage.ToolCall toolCall : toolCalls) {
            responses.add(new ToolResponseMessage.ToolResponse(
                    toolCall.id(),
                    toolCall.name(),
                    toolResponse(toolCall, callbacksByName)));
        }
        return ToolResponseMessage.builder()
                .responses(responses)
                .build();
    }

    private String toolResponse(
            AssistantMessage.ToolCall toolCall,
            Map<String, ToolCallback> callbacksByName) {
        ToolCallback callback = callbacksByName.get(toolCall.name());
        if (callback == null) {
            return UNKNOWN_TOOL_MESSAGE_PREFIX + toolCall.name();
        }
        String arguments = toolCall.arguments() == null ? EMPTY_TOOL_ARGUMENTS : toolCall.arguments();
        return callback.call(arguments);
    }
}
