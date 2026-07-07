package ai.codegeist.app.tui;

import ai.codegeist.app.i18n.CodegeistMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.jline.tui.component.message.ShellMessageBuilder;
import org.springframework.shell.jline.tui.component.view.TerminalUI;
import org.springframework.shell.jline.tui.component.view.TerminalUIBuilder;
import org.springframework.shell.jline.tui.component.view.control.BoxView;
import org.springframework.shell.jline.tui.component.view.event.KeyEvent.Key;
import org.springframework.shell.jline.tui.geom.HorizontalAlign;
import org.springframework.shell.jline.tui.geom.VerticalAlign;
import org.springframework.stereotype.Component;

/**
 * Starts the smallest current Codegeist TerminalUI surface.
 *
 * <p>This intentionally avoids a presenter, layout service, custom JLine console,
 * or wrapper layer. The current UI only proves that the Spring Shell TerminalUI
 * path can start with localized Codegeist text; future prompt submission should
 * keep using the existing chat harness instead of creating a second agent runtime.</p>
 */
@Component
@RequiredArgsConstructor
class CodegeistTerminalUi {

    private final TerminalUIBuilder terminalUIBuilder;

    private final CodegeistMessages messages;

    void run() {
        TerminalUI terminalUI = terminalUIBuilder.build();
        BoxView root = createRootView();
        terminalUI.configure(root);
        terminalUI.setRoot(root, true);
        terminalUI.getEventLoop().keyEvents().subscribe(event -> {
            if (event.getPlainKey() == Key.q && event.hasCtrl()) {
                terminalUI.getEventLoop().dispatch(ShellMessageBuilder.ofInterrupt());
            }
        });
        terminalUI.run();
    }

    BoxView createRootView() {
        BoxView root = new BoxView();
        root.setShowBorder(true);
        root.setTitle(messages.get(CodegeistMessages.TUI_TITLE_KEY));
        root.setDrawFunction((screen, rect) -> {
            screen.writerBuilder()
                    .build()
                    .text(messages.get(CodegeistMessages.TUI_QUIT_HINT_KEY), rect, HorizontalAlign.CENTER, VerticalAlign.CENTER);
            return rect;
        });
        return root;
    }
}
