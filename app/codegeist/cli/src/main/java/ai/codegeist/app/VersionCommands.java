package ai.codegeist.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class VersionCommands {

    static final String VERSION_COMMAND = "--version";

    @Autowired
    private BuildProperties buildProperties;

    @Command(name = VERSION_COMMAND, description = "Print the Codegeist version")
    void printVersion(CommandContext context) {
        log.debug("Printing Codegeist version");
        context.outputWriter().print(buildProperties.getVersion());
        context.outputWriter().flush();
    }
}
