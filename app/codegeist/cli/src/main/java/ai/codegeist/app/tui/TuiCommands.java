package ai.codegeist.app.tui;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class TuiCommands {

    static final String TUI_COMMAND = "tui";

    private final CodegeistTerminalUi terminalUi;

    @Command(name = TUI_COMMAND, description = "Open the Codegeist terminal UI")
    void tui() {
        terminalUi.run();
    }
}
