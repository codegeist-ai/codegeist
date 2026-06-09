package ai.codegeist.app.chat;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.ProviderConfig;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Arguments;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class AskCommands {

    static final String ASK_COMMAND = "ask";

    @Autowired
    private CodegeistConfig config;

    @Autowired
    private CodegeistChatService chatService;

    @Command(name = ASK_COMMAND, description = "Ask the first configured Codegeist provider")
    void ask(CommandContext context, @Arguments @NonNull String prompt) {
        ProviderConfig providerConfig = config.defaultProvider()
                .orElseThrow(() -> new IllegalStateException(CodegeistConfig.NO_PROVIDER_MESSAGE));
        String model = providerConfig.defaultModel();
        log.debug("Asking provider type {} with default model {}", providerConfig.getType(), model);
        CodegeistChatResponse response = chatService.chat(providerConfig, new CodegeistChatRequest(model, prompt));
        context.outputWriter().print(response.content());
        context.outputWriter().flush();
    }
}
