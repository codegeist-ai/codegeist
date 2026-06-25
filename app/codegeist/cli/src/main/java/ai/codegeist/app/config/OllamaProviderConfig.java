package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
public final class OllamaProviderConfig extends ProviderConfig {

    public static final String PROVIDER_TYPE = "ollama";
    public static final String DEFAULT_MODEL = "llama3.2:1b";

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
        return DEFAULT_MODEL;
    }
}
