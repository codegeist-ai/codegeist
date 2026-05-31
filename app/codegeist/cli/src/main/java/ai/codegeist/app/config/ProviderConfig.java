package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderConfig {

    private static final String NON_BLANK_WHEN_SET_PATTERN = "(?s).*\\S.*";
    private static final String NON_BLANK_WHEN_SET_MESSAGE = "must not be blank when set";

    @Pattern(regexp = NON_BLANK_WHEN_SET_PATTERN, message = NON_BLANK_WHEN_SET_MESSAGE)
    private String name;

    public ProviderConfig merge(ProviderConfig override) {
        ProviderConfig merged = new ProviderConfig();
        merged.setName(name);

        if (override == null) {
            return merged;
        }

        // T006_01 scalar precedence: later non-null values replace earlier values.
        if (override.getName() != null) {
            merged.setName(override.getName());
        }

        return merged;
    }
}
