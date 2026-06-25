package ai.codegeist.app.chat;

import ai.codegeist.app.config.OllamaProviderConfig;
import ai.codegeist.app.config.OpenAiProviderConfig;
import ai.codegeist.app.config.ProviderConfig;
import java.nio.file.Path;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

/**
 * Provider-neutral chat service that creates one provider model per request.
 *
 * <p>The public {@code chat(...)} methods keep the older one-prompt response API for
 * direct callers. The package-private {@link #rawChat(ProviderConfig,
 * CodegeistChatTurnRequest, CodegeistChatExecutionContext)} seam returns Spring AI's
 * raw response so {@link CodegeistAgentLoopService} can inspect assistant tool calls
 * before deciding whether to dispatch tools and continue.
 */
@Slf4j
@Service
public class CodegeistChatService {

    static final String UNSUPPORTED_CHAT_MODEL_MESSAGE_PREFIX = "Chat model is not implemented for provider type: ";

    public CodegeistChatResponse chat(@NonNull ProviderConfig providerConfig, @NonNull CodegeistChatRequest request) {
        return chat(providerConfig, request, CodegeistChatExecutionContext.empty(Path.of(".")));
    }

    public CodegeistChatResponse chat(
            @NonNull ProviderConfig providerConfig,
            @NonNull CodegeistChatRequest request,
            @NonNull CodegeistChatExecutionContext context) {
        return response(rawChat(providerConfig, turnRequest(request), context));
    }

    ChatResponse rawChat(
            @NonNull ProviderConfig providerConfig,
            @NonNull CodegeistChatTurnRequest request,
            @NonNull CodegeistChatExecutionContext context) {
        log.debug(
                "Creating chat model for provider type {} with {} tool callbacks",
                providerConfig.getType(),
                context.toolCallbacks().size());
        CodegeistChatModel<?> chatModel = createChatModel(providerConfig);
        return chatModel.call(request, context);
    }

    CodegeistChatModel<?> createChatModel(ProviderConfig providerConfig) {
        if (providerConfig instanceof OllamaProviderConfig ollamaProviderConfig) {
            return new OllamaChatModel(ollamaProviderConfig);
        }
        if (providerConfig instanceof OpenAiProviderConfig openAiProviderConfig) {
            return new OpenAiChatModel(openAiProviderConfig);
        }
        throw new IllegalArgumentException(UNSUPPORTED_CHAT_MODEL_MESSAGE_PREFIX + providerConfig.getType());
    }

    private CodegeistChatTurnRequest turnRequest(CodegeistChatRequest request) {
        return new CodegeistChatTurnRequest(request.model(), List.of(new UserMessage(request.prompt())));
    }

    private CodegeistChatResponse response(ChatResponse chatResponse) {
        String content = chatResponse
                .getResult()
                .getOutput()
                .getText();
        return new CodegeistChatResponse(content);
    }
}
