package ai.codegeist.app;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class TuiCommands {

    static final String TUI_COMMAND = "tui";

    private final CodegeistTerminalUi terminalUi;

    @Command(name = TUI_COMMAND, description = "Start the Codegeist TUI")
    void tui() {
        terminalUi.run();
    }
}
