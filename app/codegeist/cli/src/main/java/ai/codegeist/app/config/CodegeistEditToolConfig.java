package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Direct {@code tools.codegeist-edit:} config payload.
 *
 * <p>Both fields are optional. The runtime settings resolver applies bounded
 * defaults so malformed or non-positive values do not make edit output unbounded.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodegeistEditToolConfig extends CodegeistConfigElement {

    public static final String DIFF_PREVIEW_LINES_PROPERTY = "diff-preview-lines";
    public static final String DIFF_PREVIEW_CHARS_PROPERTY = "diff-preview-chars";

    @JsonProperty(DIFF_PREVIEW_LINES_PROPERTY)
    private Integer diffPreviewLines;

    @JsonProperty(DIFF_PREVIEW_CHARS_PROPERTY)
    private Integer diffPreviewChars;
}
