package ai.codegeist.app.chat;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistCommandExceptionMapper;
import ai.codegeist.app.CommandOutputService;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.shell.core.command.Command;
import org.springframework.shell.core.command.CommandArgument;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.CommandOption;
import org.springframework.shell.core.command.CommandRegistry;
import org.springframework.shell.core.command.DefaultCommandParser;
import org.springframework.shell.core.command.ParsedInput;

class AskCommandsSessionStoreTest {

    private static final String RESPONSE = "stub response";

    private AskCommands commands;
    private StubChatHarnessService chatHarnessService;

    @BeforeEach
    void setUp() {
        chatHarnessService = new StubChatHarnessService();
        commands = new AskCommands(chatHarnessService, new CommandOutputService());
    }

    @Test
    void plainAskPrintsHarnessResponseAndDelegatesPrompt() {
        StringWriter output = new StringWriter();

        commands.ask(commandContext(output), false, "Plain prompt");

        assertThat(output).hasToString(RESPONSE);
        assertThat(chatHarnessService.continueSession).isFalse();
        assertThat(chatHarnessService.prompt).isEqualTo("Plain prompt");
    }

    @Test
    void continueAskDelegatesContinueFlag() {
        StringWriter output = new StringWriter();

        commands.ask(commandContext(output), true, "Continue prompt");

        assertThat(output).hasToString(RESPONSE);
        assertThat(chatHarnessService.continueSession).isTrue();
        assertThat(chatHarnessService.prompt).isEqualTo("Continue prompt");
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

    private static final class StubChatHarnessService extends ChatHarnessService {

        private boolean continueSession;
        private String prompt;

        private StubChatHarnessService() {
            super(null, null, null, null, null);
        }

        @Override
        public CodegeistChatResponse ask(boolean continueSession, String prompt) {
            this.continueSession = continueSession;
            this.prompt = prompt;
            return new CodegeistChatResponse(RESPONSE);
        }
    }
}
