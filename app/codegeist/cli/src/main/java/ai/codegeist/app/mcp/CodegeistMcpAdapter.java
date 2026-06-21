package ai.codegeist.app.mcp;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.McpClientConfig;
import ai.codegeist.app.config.McpClientsRootElement;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Lazily maps Codegeist-owned MCP config into prompt-scoped Spring AI callbacks.
 *
 * <p>Config parsing, transport-type validation, Spring context startup, and
 * {@code --show-config} never call this adapter, so configured stdio processes and
 * remote MCP clients are created only for the active chat turn. Broader MCP
 * management, OAuth, resources, prompts, and server status stay outside this slice.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodegeistMcpAdapter {

    public static final String UNSUPPORTED_TYPE_PREFIX = "Unsupported MCP client type: ";

    private final CodegeistMcpClientFactory clientFactory;

    public CodegeistMcpRun openRun(@NonNull CodegeistConfig config) {
        List<McpClientConfig> clientConfigs = config.rootElement(McpClientsRootElement.class)
                .map(root -> root.getConfig().getElements())
                .orElse(List.of());
        if (clientConfigs.isEmpty()) {
            log.debug("Opened empty MCP run because no MCP clients are configured");
            return DefaultCodegeistMcpRun.empty();
        }

        List<CodegeistMcpClientHandle> handles = new ArrayList<>();
        try {
            for (McpClientConfig clientConfig : clientConfigs) {
                handles.add(clientFactory.openClient(clientConfig));
            }
            log.debug("Opened MCP run with {} configured clients", handles.size());
            return DefaultCodegeistMcpRun.fromHandles(handles);
        }
        catch (RuntimeException exception) {
            // If a later client fails to open, release every client opened so far.
            log.debug("Failed to open MCP run; closing {} previously opened clients", handles.size(), exception);
            DefaultCodegeistMcpRun.fromHandles(handles).close();
            throw exception;
        }
    }
}
