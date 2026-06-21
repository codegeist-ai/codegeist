package ai.codegeist.app.mcp;

import ai.codegeist.app.config.McpClientConfig;
import lombok.NonNull;

/**
 * Package-private seam for opening one MCP client from Codegeist config.
 *
 * <p>Unit tests replace this seam with hand-written fakes so focused adapter tests do
 * not launch stdio processes or Docker containers. The production implementation is
 * the only class that imports Spring AI MCP transport details.
 */
interface CodegeistMcpClientFactory {

    CodegeistMcpClientHandle openClient(@NonNull McpClientConfig clientConfig);
}
