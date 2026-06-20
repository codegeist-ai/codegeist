package ai.codegeist.app.chat;

import ai.codegeist.app.config.OllamaProviderConfig;
import lombok.NonNull;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;

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
            @NonNull CodegeistChatRequest request,
            @NonNull CodegeistChatExecutionContext context) {
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model(request.model())
                .toolCallbacks(context.toolCallbackArray())
                .build();
        return delegate.call(new Prompt(request.prompt(), options));
    }
}
