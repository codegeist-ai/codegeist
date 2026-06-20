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
 * <p>Both fields are optional. {@link ai.codegeist.app.tool.WorkspaceResolver}
 * decides how {@code directory} is resolved, while file tools use {@code encoding}
 * as their global text charset with a UTF-8 fallback.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceConfig extends CodegeistConfigElement {

    public static final String DIRECTORY_PROPERTY = "directory";
    public static final String ENCODING_PROPERTY = "encoding";

    @JsonProperty(DIRECTORY_PROPERTY)
    private String directory;

    @JsonProperty(ENCODING_PROPERTY)
    private Charset encoding;
}
