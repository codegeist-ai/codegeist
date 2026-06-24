package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Direct {@code tools.codegeist-shell:} config payload.
 *
 * <p>The configured command prefix is a host-side argv list. Codegeist appends the
 * model-supplied command as the final argv entry. This keeps normal shell defaults
 * small while allowing users to route execution through an explicit wrapper such as
 * Docker without teaching the tool how to manage sandboxes. The configured timeout
 * must be positive and is the per-call fallback used when a tool input omits
 * {@code timeoutSeconds} or passes a non-positive value.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodegeistShellToolConfig extends CodegeistConfigElement {

    public static final String COMMAND_PREFIX_PROPERTY = "command-prefix";
    public static final String DEFAULT_TIMEOUT_SECONDS_PROPERTY = "default-timeout-seconds";
    public static final long DEFAULT_TIMEOUT_SECONDS = 120L;

    @JsonProperty(COMMAND_PREFIX_PROPERTY)
    private List<@NotBlank String> commandPrefix = new ArrayList<>();

    @JsonProperty(DEFAULT_TIMEOUT_SECONDS_PROPERTY)
    @Positive
    private Long defaultTimeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
}
