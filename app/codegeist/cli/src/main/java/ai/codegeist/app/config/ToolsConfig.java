package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Direct {@code tools:} config root payload for Codegeist-owned local tools. */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolsConfig extends CodegeistConfigElement {

    public static final String CODEGEIST_EDIT_PROPERTY = "codegeist-edit";

    @Valid
    @JsonProperty(CODEGEIST_EDIT_PROPERTY)
    private CodegeistEditToolConfig codegeistEdit;
}
