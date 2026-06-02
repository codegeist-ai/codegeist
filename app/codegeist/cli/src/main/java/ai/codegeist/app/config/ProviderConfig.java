package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract sealed class ProviderConfig permits OllamaProviderConfig, OpenAiProviderConfig {

    static final String NON_BLANK_WHEN_SET_PATTERN = "(?s).*\\S.*";
    static final String NON_BLANK_WHEN_SET_MESSAGE = "must not be blank when set";

    @NotBlank
    private String type;

    @Pattern(regexp = NON_BLANK_WHEN_SET_PATTERN, message = NON_BLANK_WHEN_SET_MESSAGE)
    private String name;

    private Boolean enabled;

    @NotBlank
    private String model;

    private String baseUrl;

    private String completionsPath;

    private Map<String, Object> options;
}
