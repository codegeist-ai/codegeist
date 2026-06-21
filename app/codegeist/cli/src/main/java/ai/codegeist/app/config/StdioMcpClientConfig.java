package ai.codegeist.app.config;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * MCP stdio client config for local servers launched as child processes.
 */
@Getter
@Setter
public final class StdioMcpClientConfig extends McpClientConfig {

    @NotBlank
    private String command;

    private List<@NotBlank String> args = new ArrayList<>();

    public StdioMcpClientConfig() {
        setType(Type.stdio);
    }
}
