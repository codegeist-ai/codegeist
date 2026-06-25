package ai.codegeist.app.chat;

import ai.codegeist.app.config.OllamaProviderConfig;
import lombok.NonNull;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;

/**
 * Ollama-backed provider model for Codegeist chat turns.
 *
 * <p>The adapter maps Codegeist's access-only {@link OllamaProviderConfig} and the
 * per-turn runtime model into Spring AI Ollama calls. Tool callbacks are exposed to
 * the provider for tool selection, while internal Spring AI tool execution is
 * disabled so {@link CodegeistAgentLoopService} owns dispatch and continuation.
 */
public final class OllamaChatModel extends CodegeistChatModel<OllamaProviderConfig> {

    private final org.springframework.ai.ollama.OllamaChatModel delegate;

    public OllamaChatModel(OllamaProviderConfig providerConfig) {
        super(providerConfig);
        OllamaApi ollamaApi = OllamaApi.builder()
                .baseUrl(getProviderConfig().getBaseUrl())
                .build();
        delegate = org.springframework.ai.ollama.OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .build();
    }

    @Override
    public ChatResponse call(
            @NonNull CodegeistChatTurnRequest request,
            @NonNull CodegeistChatExecutionContext context) {
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model(request.model())
                .toolCallbacks(context.toolCallbacks())
                .internalToolExecutionEnabled(false)
                .build();
        return delegate.call(new Prompt(request.messages(), options));
    }
}
