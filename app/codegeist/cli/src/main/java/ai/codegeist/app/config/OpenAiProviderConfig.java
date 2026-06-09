package ai.codegeist.app.config;

import ai.codegeist.app.chat.CodegeistChatModel;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class OpenAiProviderConfig extends ProviderConfig {

    public static final String PROVIDER_TYPE = "openai";
    public static final String DEFAULT_MODEL = "gpt-5-mini";

    @NotBlank
    private String apiKey;

    private String organizationId;

    private String projectId;

    @Override
    public String getType() {
        return PROVIDER_TYPE;
    }

    @Override
    public String defaultModel() {
        return DEFAULT_MODEL;
    }

    @Override
    public CodegeistChatModel<OpenAiProviderConfig> createChatModel() {
        throw new UnsupportedOperationException("Chat model is not implemented for provider type: " + getType());
    }
}
