package ai.codegeist.app.chat;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistCommandExceptionMapper;
import ai.codegeist.app.CodegeistSpringAppProperties;
import ai.codegeist.app.CommandOutputService;
import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.ProviderConfig;
import ai.codegeist.app.session.CodegeistSession;
import ai.codegeist.app.session.SessionMessage;
import ai.codegeist.app.session.SessionMessageRole;
import ai.codegeist.app.session.SessionStore;
import ai.codegeist.app.session.SessionStoreClock;
import ai.codegeist.app.session.SessionStoreObjectMapper;
import ai.codegeist.app.session.SessionStoreService;
import ai.codegeist.app.session.TextSessionPart;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.shell.core.command.Command;
import org.springframework.shell.core.command.CommandArgument;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.CommandOption;
import org.springframework.shell.core.command.CommandRegistry;
import org.springframework.shell.core.command.DefaultCommandParser;
import org.springframework.shell.core.command.ParsedInput;
import org.springframework.test.util.ReflectionTestUtils;

class AskCommandsSessionStoreTest {

    private static final Instant CREATED_AT = Instant.parse("2026-06-09T12:00:00Z");
    private static final Instant APPEND_AT = Instant.parse("2026-06-09T12:03:00Z");
    private static final UUID LATEST_SESSION_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    @TempDir
    private Path tempDir;

    private AskCommands commands;
    private SessionStoreService sessionStoreService;
    private StubChatService chatService;

    @BeforeEach
    void setUp() {
        sessionStoreService = new SessionStoreService(new SessionStoreObjectMapper(),
                new SessionStoreClock(Clock.fixed(APPEND_AT, ZoneOffset.UTC)),
                new CodegeistSpringAppProperties());
        ReflectionTestUtils.setField(sessionStoreService, "workingDir", tempDir.toString());
        chatService = new StubChatService();
        commands = new AskCommands(new StubCodegeistConfig(), chatService, sessionStoreService,
                new CommandOutputService());
    }

    @Test
    void plainAskPrintsProviderResponseAndCreatesNewSessionStore() throws IOException {
        StringWriter output = new StringWriter();

        commands.ask(commandContext(output), false, "Plain prompt");

        assertThat(output).hasToString(StubChatService.RESPONSE);
        assertThat(chatService.request.prompt()).isEqualTo("Plain prompt");
        assertThat(Files.exists(sessionStoreService.currentStorePath())).isTrue();

        SessionStore updated = sessionStoreService.load(sessionStoreService.currentStorePath());
        assertThat(updated.getSessions()).hasSize(1);
        List<SessionMessage> messages = updated.getSessions().get(0).messages();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).role()).isEqualTo(SessionMessageRole.USER);
        assertThat(((TextSessionPart) messages.get(0).parts().get(0)).getText()).isEqualTo("Plain prompt");
        assertThat(messages.get(1).role()).isEqualTo(SessionMessageRole.ASSISTANT);
        assertThat(messages.get(1).parentMessageId()).isEqualTo(messages.get(0).id());
        assertThat(((TextSessionPart) messages.get(1).parts().get(0)).getText()).isEqualTo(StubChatService.RESPONSE);
    }

    @Test
    void continueAskAppendsPromptAndResponseToNewestSession() throws IOException {
        sessionStoreService.save(sessionStoreService.currentStorePath(), SessionStore.builder()
                .schemaVersion(SessionStore.SCHEMA_VERSION)
                .workingDir(tempDir.toString())
                .createdAt(CREATED_AT)
                .updatedAt(CREATED_AT)
                .sessions(List.of(new CodegeistSession(LATEST_SESSION_ID, "Latest", CREATED_AT, CREATED_AT, List.of())))
                .build());
        StringWriter output = new StringWriter();

        commands.ask(commandContext(output), true, "Continue prompt");

        assertThat(output).hasToString(StubChatService.RESPONSE);
        SessionStore updated = sessionStoreService.load(sessionStoreService.currentStorePath());
        List<SessionMessage> messages = updated.getSessions().get(0).messages();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).role()).isEqualTo(SessionMessageRole.USER);
        assertThat(((TextSessionPart) messages.get(0).parts().get(0)).getText()).isEqualTo("Continue prompt");
        assertThat(messages.get(1).role()).isEqualTo(SessionMessageRole.ASSISTANT);
        assertThat(messages.get(1).parentMessageId()).isEqualTo(messages.get(0).id());
        assertThat(((TextSessionPart) messages.get(1).parts().get(0)).getText()).isEqualTo(StubChatService.RESPONSE);
    }

    @Test
    void continueAskMissingSessionCreatesNewSession() throws IOException {
        StringWriter output = new StringWriter();

        commands.ask(commandContext(output), true, "Continue prompt");

        assertThat(output).hasToString(StubChatService.RESPONSE);
        SessionStore updated = sessionStoreService.load(sessionStoreService.currentStorePath());
        assertThat(updated.getSessions()).hasSize(1);
        List<SessionMessage> messages = updated.getSessions().get(0).messages();
        assertThat(messages).hasSize(2);
        assertThat(((TextSessionPart) messages.get(0).parts().get(0)).getText()).isEqualTo("Continue prompt");
        assertThat(((TextSessionPart) messages.get(1).parts().get(0)).getText()).isEqualTo(StubChatService.RESPONSE);
    }

    @Test
    void askCommandUsesSpringShellExceptionMapperAnnotation() throws NoSuchMethodException {
        org.springframework.shell.core.command.annotation.Command command = AskCommands.class
                .getDeclaredMethod("ask", CommandContext.class, boolean.class, String.class)
                .getAnnotation(org.springframework.shell.core.command.annotation.Command.class);

        assertThat(command.exitStatusExceptionMapper()).isEqualTo(CodegeistCommandExceptionMapper.BEAN_NAME);
    }

    @Test
    void springShellParserTreatsContinueOptionsAsFlags() {
        assertContinueFlagParse("ask -c \"hello world\"");
        assertContinueFlagParse("ask --continue \"hello world\"");
    }

    private void assertContinueFlagParse(String input) {
        CommandRegistry registry = new CommandRegistry();
        registry.registerCommand(Command.builder()
                .name(AskCommands.ASK_COMMAND)
                .options(CommandOption.with()
                        .shortName(AskCommands.CONTINUE_SHORT_OPTION)
                        .longName(AskCommands.CONTINUE_LONG_OPTION)
                        .type(boolean.class)
                        .build())
                .execute(context -> {}));
        ParsedInput parsedInput = new DefaultCommandParser(registry).parse(input);

        assertThat(parsedInput.options()).singleElement().satisfies(option -> {
            assertThat(option.value()).isEqualTo("true");
            assertThat(option.isOptionEqual("-c") || option.isOptionEqual("--continue")).isTrue();
        });
        assertThat(parsedInput.arguments()).singleElement()
                .extracting(CommandArgument::value)
                .isEqualTo("hello world");
    }

    private CommandContext commandContext(StringWriter output) {
        return new CommandContext(
                ParsedInput.builder().commandName(AskCommands.ASK_COMMAND).build(),
                null,
                new PrintWriter(output),
                null);
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
            return "stub-model";
        }

        @Override
        public CodegeistChatModel<?> createChatModel() {
            throw new UnsupportedOperationException("Stub chat service should handle requests");
        }
    }

    private static final class StubChatService extends CodegeistChatService {

        static final String RESPONSE = "stub response";

        private CodegeistChatRequest request;

        @Override
        public CodegeistChatResponse chat(ProviderConfig providerConfig, CodegeistChatRequest request) {
            this.request = request;
            return new CodegeistChatResponse(RESPONSE);
        }
    }
}
