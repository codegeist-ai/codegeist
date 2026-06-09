package ai.codegeist.app.config;

import ai.codegeist.app.chat.CodegeistChatModel;

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
public abstract class ProviderConfig extends CodegeistConfigElement {

    static final String NON_BLANK_WHEN_SET_PATTERN = "(?s).*\\S.*";
    static final String NON_BLANK_WHEN_SET_MESSAGE = "must not be blank when set";

    @Pattern(regexp = NON_BLANK_WHEN_SET_PATTERN, message = NON_BLANK_WHEN_SET_MESSAGE)
    private String name;

    private String baseUrl;

    @JsonProperty(value = "type", access = JsonProperty.Access.READ_ONLY)
    @Override
    public abstract String getType();

    public abstract String defaultModel();

    public abstract CodegeistChatModel<?> createChatModel();
}
