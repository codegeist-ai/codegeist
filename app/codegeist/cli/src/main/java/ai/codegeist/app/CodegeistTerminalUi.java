package ai.codegeist.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.jline.tui.component.view.TerminalUI;
import org.springframework.shell.jline.tui.component.view.TerminalUIBuilder;
import org.springframework.shell.jline.tui.component.view.control.ListView;
import org.springframework.stereotype.Component;

/**
 * Starts the first minimal Codegeist-owned Spring Shell TerminalUI surface.
 *
 * <p>The current TUI intentionally contains only a static root view so the
 * {@code codegeist tui} command can prove TerminalUI wiring before later tasks add
 * session projection, chat flow, or rendering logic.</p>
 */
@Component
@RequiredArgsConstructor
class CodegeistTerminalUi {

    static final String GREETING = "hello tui";

    private final TerminalUIBuilder terminalUIBuilder;

    void run() {
        TerminalUI terminalUI = terminalUIBuilder.build();
        ListView<String> root = new ListView<>(List.of(GREETING), ListView.ItemStyle.NOCHECK);
        root.setTitle("Codegeist");
        terminalUI.setRoot(root, true);
        terminalUI.run();
    }
}
