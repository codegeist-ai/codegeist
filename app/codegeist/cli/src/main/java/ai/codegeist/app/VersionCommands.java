package ai.codegeist.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class VersionCommands {

    static final String VERSION_COMMAND = "--version";

    private final BuildProperties buildProperties;

    private final CommandOutputService outputService;

    @Command(name = VERSION_COMMAND, description = "Print the Codegeist version")
    void printVersion(CommandContext context) {
        log.debug("Printing Codegeist version");
        outputService.print(context, buildProperties.getVersion());
    }
}
