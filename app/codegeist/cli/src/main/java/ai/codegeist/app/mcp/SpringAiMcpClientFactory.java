package ai.codegeist.app.mcp;

import ai.codegeist.app.config.McpClientConfig;
import ai.codegeist.app.config.StdioMcpClientConfig;
import ai.codegeist.app.config.StreamableHttpMcpClientConfig;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

/**
 * Production MCP client factory backed by Spring AI and the MCP Java SDK.
 *
 * <p>
 * This class is intentionally the only Codegeist runtime class that imports MCP
 * transport implementations. Milestone API churn should stay localized here
 * while
 * {@link CodegeistMcpAdapter} and the tool harness continue to work with
 * ordinary
 * Spring AI {@link ToolCallback} values.
 */
@Slf4j
@Component
final class SpringAiMcpClientFactory implements CodegeistMcpClientFactory {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final McpJsonMapper jsonMapper = new JacksonMcpJsonMapper(JsonMapper.builder().build());

    @Override
    public CodegeistMcpClientHandle openClient(@NonNull McpClientConfig clientConfig) {
        McpSyncClient client = createClient(clientConfig);
        try {
            client.initialize();
            List<ToolCallback> callbacks = SyncMcpToolCallbackProvider.syncToolCallbacks(List.of(client));
            log.debug("Opened MCP client {} with {} callbacks", clientConfig.getId(), callbacks.size());
            return new CodegeistMcpClientHandle(callbacks, client);
        }
        catch (RuntimeException exception) {
            client.close();
            throw exception;
        }
    }

    private McpSyncClient createClient(McpClientConfig clientConfig) {
        return McpClient.sync(switch (clientConfig) {
            case StdioMcpClientConfig stdioConfig -> stdioTransport(stdioConfig);
            case StreamableHttpMcpClientConfig httpConfig -> streamableHttpTransport(httpConfig);
            default -> throw new IllegalStateException(
                    CodegeistMcpAdapter.UNSUPPORTED_TYPE_PREFIX + clientConfig.getType());
        })
                .requestTimeout(REQUEST_TIMEOUT)
                .initializationTimeout(REQUEST_TIMEOUT)
                .build();
    }

    private StdioClientTransport stdioTransport(StdioMcpClientConfig clientConfig) {
        ServerParameters parameters = ServerParameters.builder(clientConfig.getCommand())
                .args(clientConfig.getArgs())
                .build();
        return new StdioClientTransport(parameters, jsonMapper);
    }

    private HttpClientStreamableHttpTransport streamableHttpTransport(StreamableHttpMcpClientConfig clientConfig) {
        HttpClientStreamableHttpTransport.Builder builder = HttpClientStreamableHttpTransport.builder(clientConfig.getUrl())
                .clientBuilder(HttpClient.newBuilder())
                .jsonMapper(jsonMapper);

        if (StringUtils.hasText(clientConfig.getEndpoint())) {
            builder.endpoint(clientConfig.getEndpoint());
        }

        return builder.build();
    }
}
