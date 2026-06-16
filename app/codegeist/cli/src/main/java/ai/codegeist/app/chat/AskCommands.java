package ai.codegeist.app.chat;

import ai.codegeist.app.CodegeistCommandExceptionMapper;
import ai.codegeist.app.CommandOutputService;
import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.ProviderConfig;
import ai.codegeist.app.session.SessionStoreService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Arguments;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class AskCommands {

    static final String ASK_COMMAND = "ask";
    static final char CONTINUE_SHORT_OPTION = 'c';
    static final String CONTINUE_LONG_OPTION = "continue";

    private final CodegeistConfig config;

    private final CodegeistChatService chatService;

    private final SessionStoreService sessionStoreService;

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
        ProviderConfig providerConfig = config.defaultProvider()
                .orElseThrow(() -> new IllegalStateException(CodegeistConfig.NO_PROVIDER_MESSAGE));
        String model = providerConfig.defaultModel();
        log.debug("Asking provider type {} with default model {}", providerConfig.getType(), model);
        CodegeistChatResponse response = chatService.chat(providerConfig, new CodegeistChatRequest(model, prompt));
        sessionStoreService.saveExchangeToCurrentSession(continueSession, prompt, response.content());
        outputService.print(context, response.content());
    }
}
