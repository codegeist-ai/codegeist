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
@Provider("ollama")
public final class OllamaProviderConfig extends ProviderConfig {

    @AssertTrue(message = "base-url must not be blank")
    @JsonIgnore
    public boolean isBaseUrlConfigured() {
        return StringUtils.hasText(getBaseUrl());
    }
}
