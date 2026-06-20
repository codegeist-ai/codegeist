package ai.codegeist.app.chat;

import ai.codegeist.app.CodegeistCommandExceptionMapper;
import ai.codegeist.app.CommandOutputService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Arguments;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AskCommands {

    static final String ASK_COMMAND = "ask";
    static final char CONTINUE_SHORT_OPTION = 'c';
    static final String CONTINUE_LONG_OPTION = "continue";

    private final ChatHarnessService chatHarnessService;

    private final CommandOutputService outputService;

    @Command(
            name = ASK_COMMAND,
            description = "Ask the first configured Codegeist provider",
            exitStatusExceptionMapper = CodegeistCommandExceptionMapper.BEAN_NAME)
    void ask(
            CommandContext context,
            @Option(shortName = CONTINUE_SHORT_OPTION, longName = CONTINUE_LONG_OPTION,
                    description = "Continue the newest session in .codegeist/session.json") boolean continueSession,
            @Arguments @NonNull String prompt) {
        CodegeistChatResponse response = chatHarnessService.ask(continueSession, prompt);
        outputService.print(context, response.content());
    }
}
