package ai.codegeist.app.chat;

import ai.codegeist.app.config.OpenAiProviderConfig;
import java.util.Map;
import lombok.NonNull;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.util.StringUtils;

/**
 * OpenAI-backed provider model for Codegeist chat turns.
 *
 * <p>The adapter maps Codegeist's access-only {@link OpenAiProviderConfig} and the
 * per-turn runtime model into Spring AI OpenAI calls. Like the Ollama adapter, it
 * exposes prompt-scoped tool callbacks to the provider only as tool definitions and
 * keeps Spring AI internal tool execution disabled so {@link CodegeistAgentLoopService}
 * remains the only dispatcher for tool calls and continuation turns.
 */
public final class OpenAiChatModel extends CodegeistChatModel<OpenAiProviderConfig> {

    private static final String OPENAI_PROJECT_HEADER = "OpenAI-Project";

    private final org.springframework.ai.openai.OpenAiChatModel delegate;

    public OpenAiChatModel(OpenAiProviderConfig providerConfig) {
        super(providerConfig);
        delegate = org.springframework.ai.openai.OpenAiChatModel.builder()
                .options(accessOptionsBuilder().build())
                .build();
    }

    @Override
    public ChatResponse call(
            @NonNull CodegeistChatTurnRequest request,
            @NonNull CodegeistChatExecutionContext context) {
        OpenAiChatOptions options = accessOptionsBuilder()
                .model(request.model())
                .toolCallbacks(context.toolCallbacks())
                .internalToolExecutionEnabled(false)
                .build();
        return delegate.call(new Prompt(request.messages(), options));
    }

    private OpenAiChatOptions.Builder accessOptionsBuilder() {
        OpenAiProviderConfig providerConfig = getProviderConfig();
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder()
                .apiKey(providerConfig.getApiKey());
        if (StringUtils.hasText(providerConfig.getBaseUrl())) {
            builder.baseUrl(providerConfig.getBaseUrl());
        }
        if (StringUtils.hasText(providerConfig.getOrganizationId())) {
            builder.organizationId(providerConfig.getOrganizationId());
        }
        if (StringUtils.hasText(providerConfig.getProjectId())) {
            builder.customHeaders(Map.of(OPENAI_PROJECT_HEADER, providerConfig.getProjectId()));
        }
        return builder;
    }
}
