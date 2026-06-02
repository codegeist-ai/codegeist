package ai.codegeist.app.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Provider("openai")
public final class OpenAiProviderConfig extends ProviderConfig {

    @NotBlank
    private String apiKey;

    private String organizationId;

    private String projectId;
}
