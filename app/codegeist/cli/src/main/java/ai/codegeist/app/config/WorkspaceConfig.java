package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.nio.charset.Charset;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Direct {@code workspace:} config root payload.
 *
 * <p>All fields are optional. {@link ai.codegeist.app.tool.WorkspaceResolver}
 * decides how {@code directory} is resolved, file tools use {@code encoding} as
 * their global text charset with a UTF-8 fallback, and side-effecting tool guards
 * are enabled unless {@code dir-guard-disabled} is explicitly true.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceConfig extends CodegeistConfigElement {

    public static final String DIRECTORY_PROPERTY = "directory";
    public static final String ENCODING_PROPERTY = "encoding";
    public static final String DIR_GUARD_DISABLED_PROPERTY = "dir-guard-disabled";

    @JsonProperty(DIRECTORY_PROPERTY)
    private String directory;

    @JsonProperty(ENCODING_PROPERTY)
    private Charset encoding;

    @JsonProperty(DIR_GUARD_DISABLED_PROPERTY)
    private Boolean dirGuardDisabled;
}
