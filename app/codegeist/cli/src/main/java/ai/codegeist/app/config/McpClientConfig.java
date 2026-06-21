package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base class for direct {@code codegeist.yml} MCP client definitions.
 *
 * <p>MCP clients are represented as keyed YAML object entries under {@code mcp:},
 * for example {@code mcp.filesystem}. The parser copies that key into this internal
 * {@code id} field while the root model stores clients as a list. Concrete subclasses
 * own the transport-specific required fields for {@code stdio} and
 * {@code streamable_http}.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class McpClientConfig extends CodegeistTypedConfigElement<McpClientConfig.Type> {

    public enum Type {
        stdio,
        streamable_http
    }

    @NotBlank
    @JsonIgnore
    private String id;
}
