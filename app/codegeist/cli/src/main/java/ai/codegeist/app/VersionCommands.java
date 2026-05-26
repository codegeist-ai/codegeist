package ai.codegeist.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

@Component
class VersionCommands {

    static final String VERSION_COMMAND = "--version";

    @Autowired
    private BuildProperties buildProperties;

    @Command(name = VERSION_COMMAND, description = "Print the Codegeist version")
    void printVersion(CommandContext context) {
        context.outputWriter().print(buildProperties.getVersion());
        context.outputWriter().flush();
    }
}
