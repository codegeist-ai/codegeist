package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Minimal direct {@code codegeist.yml} MCP client definition.
 *
 * <p>MCP clients are represented as keyed YAML object entries under {@code mcp:},
 * for example {@code mcp.filesystem}. The parser copies that key into this internal
 * {@code id} field while the root model stores clients as a list. {@code type}
 * remains only the transport kind, such as {@code stdio}, so multiple clients can
 * share the same transport type without collision.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class McpClientConfig extends CodegeistTypedConfigElement {

    @NotBlank
    @JsonIgnore
    private String id;

    @NotBlank
    private String command;

    private List<@NotBlank String> args = new ArrayList<>();
}
