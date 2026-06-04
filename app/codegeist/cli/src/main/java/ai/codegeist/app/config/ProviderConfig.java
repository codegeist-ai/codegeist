package ai.codegeist.app.config;

import ai.codegeist.app.chat.CodegeistChatModel;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ProviderConfig {

    static final String NON_BLANK_WHEN_SET_PATTERN = "(?s).*\\S.*";
    static final String NON_BLANK_WHEN_SET_MESSAGE = "must not be blank when set";

    @Pattern(regexp = NON_BLANK_WHEN_SET_PATTERN, message = NON_BLANK_WHEN_SET_MESSAGE)
    private String name;

    private String baseUrl;

    @JsonProperty(value = "type", access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        Provider provider = getClass().getAnnotation(Provider.class);
        if (provider == null) {
            throw new IllegalStateException("Missing @Provider on " + getClass().getName());
        }
        return provider.value();
    }

    public abstract String defaultModel();

    public abstract CodegeistChatModel<?> createChatModel();
}
