package ai.codegeist.app.mcp;

import ai.codegeist.app.config.McpClientConfig;
import java.util.List;
import lombok.NonNull;
import org.springframework.ai.tool.ToolCallback;

public final class TestMcpAdapters {

    private TestMcpAdapters() {
    }

    public static CodegeistMcpAdapter empty() {
        return withRun(DefaultCodegeistMcpRun.empty());
    }

    public static CodegeistMcpAdapter withRun(@NonNull CodegeistMcpRun run) {
        return new CodegeistMcpAdapter(new StaticMcpClientFactory(run));
    }

    private static final class StaticMcpClientFactory implements CodegeistMcpClientFactory {

        private final CodegeistMcpRun run;

        private StaticMcpClientFactory(CodegeistMcpRun run) {
            this.run = run;
        }

        @Override
        public CodegeistMcpClientHandle openClient(McpClientConfig clientConfig) {
            return new CodegeistMcpClientHandle(run.getToolCallbacks(), run::close);
        }
    }

    public static CodegeistMcpRun runWithCallbacks(List<ToolCallback> callbacks) {
        return new DefaultCodegeistMcpRun(callbacks, List.of());
    }
}
