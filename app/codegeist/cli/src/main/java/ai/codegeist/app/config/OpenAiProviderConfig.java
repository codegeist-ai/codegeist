package ai.codegeist.app.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Access-only OpenAI provider config for Codegeist chat runtime and provider tests.
 *
 * <p>The config stores credentials and optional routing headers only. It deliberately
 * does not store model names, generation options, or chat adapter factories; command
 * and runtime callers select the model through
 * {@link ai.codegeist.app.chat.CodegeistChatRequest}, while the chat service owns
 * provider-adapter creation for the selected provider.
 */
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
}
