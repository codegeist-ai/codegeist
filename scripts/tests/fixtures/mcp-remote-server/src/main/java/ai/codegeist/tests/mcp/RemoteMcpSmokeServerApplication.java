package ai.codegeist.tests.mcp;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

/**
 * Minimal Spring Boot MCP server used only by the Docker-backed remote smoke test.
 *
 * <p>The fixture exposes deterministic tools over Streamable HTTP so Codegeist can
 * prove its remote MCP client path without using hosted networks or an LLM provider.
 */
@SpringBootApplication
public class RemoteMcpSmokeServerApplication {

    private static final String MCP_ENDPOINT = "/mcp";
    private static final String TOOL_NAME = "remote_echo";
    private static final String TOOL_INPUT_SCHEMA = """
            {"type":"object","properties":{"text":{"type":"string"}},"required":["text"]}
            """;

    public static void main(String[] args) {
        SpringApplication.run(RemoteMcpSmokeServerApplication.class, args);
    }

    @Bean
    McpJsonMapper mcpJsonMapper() {
        return new JacksonMcpJsonMapper(JsonMapper.builder().build());
    }

    @Bean
    HttpServletStreamableServerTransportProvider mcpTransportProvider(McpJsonMapper jsonMapper) {
        return HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(jsonMapper)
                .mcpEndpoint(MCP_ENDPOINT)
                .build();
    }

    @Bean
    ServletRegistrationBean<HttpServletStreamableServerTransportProvider> mcpServlet(
            HttpServletStreamableServerTransportProvider transportProvider) {
        return new ServletRegistrationBean<>(transportProvider, MCP_ENDPOINT);
    }

    @Bean
    McpSyncServer mcpServer(
            HttpServletStreamableServerTransportProvider transportProvider,
            McpJsonMapper jsonMapper,
            RemoteMcpSmokeTools tools) {
        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name(TOOL_NAME)
                .description("Echo text for Codegeist remote MCP smoke tests")
                .inputSchema(jsonMapper, TOOL_INPUT_SCHEMA)
                .build();
        McpServerFeatures.SyncToolSpecification toolSpecification =
                McpServerFeatures.SyncToolSpecification.builder()
                        .tool(tool)
                        .callHandler((exchange, request) -> McpSchema.CallToolResult.builder()
                                .addTextContent(tools.remoteEcho(String.valueOf(request.arguments().get("text"))))
                                .isError(false)
                                .build())
                        .build();
        return McpServer.sync(transportProvider)
                .serverInfo("codegeist-mcp-remote-smoke-server", "0.1.0-SNAPSHOT")
                .instructions("Deterministic MCP fixture for Codegeist remote smoke tests.")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
                .tools(toolSpecification)
                .build();
    }
}
