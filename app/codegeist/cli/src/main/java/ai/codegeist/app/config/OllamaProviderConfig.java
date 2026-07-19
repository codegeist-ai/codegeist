package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 * Local Ollama connection settings and command-default model selection. A validated
 * YAML {@code model} overrides the compatibility fallback used by commands that do
 * not expose their own model selector.
 */
@Getter
@Setter
@NoArgsConstructor
public final class OllamaProviderConfig extends ProviderConfig {

    public static final String PROVIDER_TYPE = "ollama";
    public static final String DEFAULT_MODEL = "llama3.2:1b";

    @Pattern(regexp = NON_BLANK_WHEN_SET_PATTERN, message = NON_BLANK_WHEN_SET_MESSAGE)
    private String model;

    @AssertTrue(message = "base-url must not be blank")
    @JsonIgnore
    public boolean isBaseUrlConfigured() {
        return StringUtils.hasText(getBaseUrl());
    }

    @Override
    public String getType() {
        return PROVIDER_TYPE;
    }

    @Override
    public String defaultModel() {
        return model == null ? DEFAULT_MODEL : model;
    }
}
