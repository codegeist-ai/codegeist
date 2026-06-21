package ai.codegeist.app.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * MCP Streamable HTTP client config for remote MCP servers.
 *
 * <p>The optional {@code endpoint} is passed to the MCP Java SDK builder only when
 * configured. When it is absent or blank, the SDK-owned endpoint default is used.
 */
@Getter
@Setter
public final class StreamableHttpMcpClientConfig extends McpClientConfig {

    @NotBlank
    private String url;

    private String endpoint;

    public StreamableHttpMcpClientConfig() {
        setType(Type.streamable_http);
    }
}
