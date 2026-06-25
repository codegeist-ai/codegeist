package ai.codegeist.app.chat;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistSpringAppProperties;
import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.ProviderConfig;
import ai.codegeist.app.mcp.TestMcpAdapters;
import ai.codegeist.app.session.SessionMessage;
import ai.codegeist.app.session.SessionMessageRole;
import ai.codegeist.app.session.SessionPart;
import ai.codegeist.app.session.SessionStore;
import ai.codegeist.app.session.SessionStoreClock;
import ai.codegeist.app.session.SessionStoreObjectMapper;
import ai.codegeist.app.session.SessionStoreService;
import ai.codegeist.app.session.TextSessionPart;
import ai.codegeist.app.session.ToolSessionPart;
import ai.codegeist.app.session.ToolSessionPart.ToolSessionPartStatus;
import ai.codegeist.app.tool.CodegeistToolRun;
import ai.codegeist.app.tool.CodegeistToolService;
import ai.codegeist.app.tool.WorkspaceResolver;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.test.util.ReflectionTestUtils;

class ChatHarnessServiceTest {

    private static final Instant APPEND_AT = Instant.parse("2026-06-20T12:00:00Z");
    private static final String PROMPT = "Read README";
    private static final String RESPONSE = "The README was read.";
    private static final String MODEL = "stub-model";
    private static final String TOOL_NAME = "fake_tool";
    private static final String TOOL_OUTPUT = "fake tool output";

    @TempDir
    private Path tempDir;

    private SessionStoreService sessionStoreService;
    private StubCodegeistConfig config;
    private StubCodegeistAgentLoopService agentLoopService;
    private StubCodegeistToolService toolService;
    private ChatHarnessService harnessService;

    @BeforeEach
    void setUp() {
        sessionStoreService = new SessionStoreService(new SessionStoreObjectMapper(),
                new SessionStoreClock(Clock.fixed(APPEND_AT, ZoneOffset.UTC)),
                new CodegeistSpringAppProperties());
        ReflectionTestUtils.setField(sessionStoreService, "workingDir", tempDir.toString());
        config = new StubCodegeistConfig();
        agentLoopService = new StubCodegeistAgentLoopService();
        toolService = new StubCodegeistToolService();
        harnessService = new ChatHarnessService(
                config,
                agentLoopService,
                toolService,
                new StubWorkspaceResolver(config, tempDir),
                sessionStoreService);
    }

    @Test
    void asksProviderWithToolContextAndSavesRecordedToolParts() throws Exception {
        CodegeistChatResponse response = harnessService.ask(true, PROMPT);

        assertThat(response.content()).isEqualTo(RESPONSE);
        assertThat(agentLoopService.providerConfig).isSameAs(config.providerConfig);
        assertThat(agentLoopService.request).isEqualTo(new CodegeistChatRequest(MODEL, PROMPT));
        assertThat(agentLoopService.context.workingDirectory()).isEqualTo(tempDir.toAbsolutePath().normalize());

        SessionStore updated = sessionStoreService.load(sessionStoreService.currentStorePath());
        assertThat(updated.getSessions()).hasSize(1);
        List<SessionMessage> messages = updated.getSessions().get(0).messages();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).role()).isEqualTo(SessionMessageRole.USER);
        assertThat(((TextSessionPart) messages.get(0).parts().get(0)).getText()).isEqualTo(PROMPT);

        SessionMessage assistantMessage = messages.get(1);
        assertThat(assistantMessage.role()).isEqualTo(SessionMessageRole.ASSISTANT);
        assertThat(assistantMessage.parentMessageId()).isEqualTo(messages.get(0).id());
        assertThat(assistantMessage.parts()).hasSize(2);
        SessionPart toolPart = assistantMessage.parts().get(0);
        assertThat(toolPart).isInstanceOf(ToolSessionPart.class);
        ToolSessionPart savedToolPart = (ToolSessionPart) toolPart;
        assertThat(savedToolPart.getTool()).isEqualTo(TOOL_NAME);
        assertThat(savedToolPart.getStatus()).isEqualTo(ToolSessionPartStatus.completed);
        assertThat(savedToolPart.getOutputPreview()).isEqualTo(TOOL_OUTPUT);
        assertThat(((TextSessionPart) assistantMessage.parts().get(1)).getText()).isEqualTo(RESPONSE);
        assertThat(toolService.run.closed).isTrue();
    }

    private static final class StubCodegeistConfig extends CodegeistConfig {

        private final ProviderConfig providerConfig = new StubProviderConfig();

        @Override
        public Optional<ProviderConfig> defaultProvider() {
            return Optional.of(providerConfig);
        }
    }

    private static final class StubProviderConfig extends ProviderConfig {

        @Override
        public String getType() {
            return "stub";
        }

        @Override
        public String defaultModel() {
            return MODEL;
        }
    }

    private static final class StubWorkspaceResolver extends WorkspaceResolver {

        private final Path workspace;

        private StubWorkspaceResolver(CodegeistConfig config, Path workspace) {
            super(config);
            this.workspace = workspace;
        }

        @Override
        public Path currentWorkspace() {
            return workspace.toAbsolutePath().normalize();
        }
    }

    private static final class StubCodegeistToolService extends CodegeistToolService {

        private StubCodegeistToolRun run;

        private StubCodegeistToolService() {
            super(null, null, TestMcpAdapters.empty());
        }

        @Override
        public CodegeistToolRun openRun(CodegeistConfig config, Path workingDirectory) {
            run = new StubCodegeistToolRun(workingDirectory);
            return run;
        }
    }

    private static final class StubCodegeistToolRun implements CodegeistToolRun {

        private final List<ToolSessionPart> completedToolParts = new ArrayList<>();
        private final CodegeistChatExecutionContext executionContext;
        private boolean closed;

        private StubCodegeistToolRun(Path workingDirectory) {
            executionContext = new CodegeistChatExecutionContext(
                    workingDirectory,
                    List.of(new RecordingToolCallback(completedToolParts)));
        }

        @Override
        public CodegeistChatExecutionContext executionContext() {
            return executionContext;
        }

        @Override
        public List<ToolSessionPart> completedToolParts() {
            return List.copyOf(completedToolParts);
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    private static final class RecordingToolCallback implements ToolCallback {

        private final List<ToolSessionPart> completedToolParts;

        private RecordingToolCallback(List<ToolSessionPart> completedToolParts) {
            this.completedToolParts = completedToolParts;
        }

        @Override
        public ToolDefinition getToolDefinition() {
            return ToolDefinition.builder()
                    .name(TOOL_NAME)
                    .description("Fake tool")
                    .inputSchema("{\"type\":\"object\"}")
                    .build();
        }

        @Override
        public ToolMetadata getToolMetadata() {
            return ToolMetadata.builder().returnDirect(false).build();
        }

        @Override
        public String call(String toolInput) {
            ToolSessionPart part = new ToolSessionPart();
            part.setTool(TOOL_NAME);
            part.setStatus(ToolSessionPartStatus.completed);
            part.setOutputPreview(TOOL_OUTPUT);
            completedToolParts.add(part);
            return TOOL_OUTPUT;
        }

        @Override
        public String call(String toolInput, ToolContext toolContext) {
            return call(toolInput);
        }
    }

    private static final class StubCodegeistAgentLoopService extends CodegeistAgentLoopService {

        private ProviderConfig providerConfig;
        private CodegeistChatRequest request;
        private CodegeistChatExecutionContext context;

        private StubCodegeistAgentLoopService() {
            super(null);
        }

        @Override
        public CodegeistChatResponse run(
                ProviderConfig providerConfig,
                CodegeistChatRequest request,
                CodegeistChatExecutionContext context) {
            this.providerConfig = providerConfig;
            this.request = request;
            this.context = context;
            assertThat(context.toolCallbacks()).singleElement().satisfies(callback -> callback.call("{}"));
            return new CodegeistChatResponse(RESPONSE);
        }
    }
}
