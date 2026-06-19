package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Direct {@code workspace:} config root payload.
 *
 * <p>Only {@code directory} exists in the first workspace slice. It is optional and
 * nullable; {@link ai.codegeist.app.tool.WorkspaceResolver} decides whether the
 * configured value is usable and how relative paths are resolved.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceConfig extends CodegeistConfigElement {

    public static final String DIRECTORY_PROPERTY = "directory";

    @JsonProperty(DIRECTORY_PROPERTY)
    private String directory;
}
