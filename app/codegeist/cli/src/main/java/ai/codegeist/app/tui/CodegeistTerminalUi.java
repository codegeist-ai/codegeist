package ai.codegeist.app.tui;

import ai.codegeist.app.chat.ChatHarnessService;
import ai.codegeist.app.chat.CodegeistChatResponse;
import ai.codegeist.app.i18n.CodegeistMessages;
import ai.codegeist.app.session.ToolSessionPart;
import java.io.IOError;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.jline.tui.component.message.ShellMessageBuilder;
import org.springframework.shell.jline.tui.component.view.TerminalUI;
import org.springframework.shell.jline.tui.component.view.TerminalUIBuilder;
import org.springframework.shell.jline.tui.component.view.control.BoxView;
import org.springframework.shell.jline.tui.component.view.control.GridView;
import org.springframework.shell.jline.tui.component.view.control.InputView;
import org.springframework.shell.jline.tui.component.view.control.InputView.InputViewTextChangeEvent;
import org.springframework.shell.jline.tui.component.view.control.ViewDoneEvent;
import org.springframework.shell.jline.tui.component.view.event.KeyEvent;
import org.springframework.shell.jline.tui.component.view.event.KeyEvent.Key;
import org.springframework.shell.jline.tui.component.view.event.KeyHandler;
import org.springframework.shell.jline.tui.geom.HorizontalAlign;
import org.springframework.shell.jline.tui.geom.Rectangle;
import org.springframework.shell.jline.tui.geom.VerticalAlign;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Starts the current Codegeist TerminalUI chat surface.
 *
 * <p>This intentionally avoids a presenter, layout service, custom JLine console,
 * or wrapper layer. The UI keeps only process-local transcript projection and routes
 * prompt turns through {@link ChatHarnessService}, so provider, tool, agent-loop, and
 * session-store behavior stay behind the existing runtime harness.</p>
 */
@Component
@RequiredArgsConstructor
class CodegeistTerminalUi {

    private static final boolean CONTINUE_TUI_SESSION = true;

    private static final int PROMPT_ROW_HEIGHT = 3;

    private static final String LABEL_SEPARATOR = ": ";

    private static final String COMMAND_FAILED_PREFIX = "Command failed: ";

    private static final String INPUT_SPACE_SENTINEL = String.valueOf((char) 0x00A0);

    private static final String SPACE = " ";

    private final TerminalUIBuilder terminalUIBuilder;

    private final CodegeistMessages messages;

    private final ChatHarnessService chatHarnessService;

    void run() {
        TuiChatState state = new TuiChatState();
        // Spring Shell can return from TerminalUI.run() after a prompt completes;
        // keep the Codegeist TUI process alive while preserving transcript state.
        while (!state.quitRequested()) {
            TerminalUI terminalUI = terminalUIBuilder.build();
            installChatSurface(terminalUI, state);
            registerChatEventHandlers(terminalUI, state);
            try {
                terminalUI.run();
            }
            catch (IOError error) {
                handleTerminalRunError(state, error);
            }
        }
    }

    void handleTerminalRunError(TuiChatState state, IOError error) {
        if (!isTerminalInterrupt(error)) {
            throw error;
        }
        // TerminalUI can surface an interrupt after a prompt completes. Only the
        // Ctrl-Q key handler sets quitRequested; other interrupts should let the
        // outer loop rebuild the surface and keep the transcript visible.
        Thread.interrupted();
    }

    private void registerChatEventHandlers(TerminalUI terminalUI, TuiChatState state) {
        terminalUI.getEventLoop().onDestroy(terminalUI.getEventLoop().keyEvents().subscribe(event -> {
            if (event.getPlainKey() == Key.q && event.hasCtrl()) {
                state.quitRequested(true);
                terminalUI.getEventLoop().dispatch(ShellMessageBuilder.ofInterrupt());
            }
        }));
        terminalUI.getEventLoop().onDestroy(terminalUI.getEventLoop()
                .viewEvents(InputViewTextChangeEvent.class)
                .subscribe(event -> {
                    if (event.view() == state.activePromptInput()) {
                        terminalUI.redraw();
                    }
                }));
        terminalUI.getEventLoop().onDestroy(terminalUI.getEventLoop()
                .viewEvents(ViewDoneEvent.class)
                .subscribe(event -> {
                    InputView activePromptInput = state.activePromptInput();
                    if (event.view() == activePromptInput) {
                        submitPrompt(terminalUI, state, activePromptInput.getInputText());
                    }
                }));
    }

    TuiChatSurface installChatSurface(TerminalUI terminalUI, TuiChatState state) {
        BoxView transcriptView = createTranscriptView(state);
        InputView promptInput = createPromptInputView();
        GridView root = createRootView(transcriptView, promptInput);
        terminalUI.configure(transcriptView);
        terminalUI.configure(promptInput);
        terminalUI.configure(root);
        terminalUI.setRoot(root, true);
        terminalUI.setFocus(promptInput);
        TuiChatSurface surface = new TuiChatSurface(root, transcriptView, promptInput);
        state.activeSurface(surface);
        state.activePromptInput(promptInput);
        return surface;
    }

    void resetPromptInput(TerminalUI terminalUI, TuiChatState state) {
        TuiChatSurface surface = state.activeSurface();
        if (surface == null) {
            installChatSurface(terminalUI, state);
            terminalUI.redraw();
            return;
        }

        InputView promptInput = createPromptInputView();
        surface.root().clearItems();
        surface.root().addItem(surface.transcriptView(), 0, 0, 1, 1, 0, 0);
        surface.root().addItem(promptInput, 1, 0, 1, 1, 0, 0);
        terminalUI.configure(promptInput);
        terminalUI.setFocus(promptInput);
        state.activeSurface(new TuiChatSurface(surface.root(), surface.transcriptView(), promptInput));
        state.activePromptInput(promptInput);
        terminalUI.redraw();
    }

    void submitPrompt(TerminalUI terminalUI, TuiChatState state, String promptText) {
        if (!StringUtils.hasText(promptText)) {
            resetPromptInput(terminalUI, state);
            return;
        }

        state.addTranscriptLine(label(CodegeistMessages.TUI_USER_LABEL_KEY, promptText));
        try {
            CodegeistChatResponse response = chatHarnessService.ask(CONTINUE_TUI_SESSION, promptText);
            addToolTranscriptLines(state, response.toolParts());
            state.addTranscriptLine(label(CodegeistMessages.TUI_ASSISTANT_LABEL_KEY, response.content()));
        }
        catch (RuntimeException exception) {
            state.addTranscriptLine(label(CodegeistMessages.TUI_ERROR_LABEL_KEY, commandFailureMessage(exception)));
        }
        resetPromptInput(terminalUI, state);
    }

    private void addToolTranscriptLines(TuiChatState state, List<ToolSessionPart> toolParts) {
        for (ToolSessionPart toolPart : toolParts) {
            state.addTranscriptLine(toolLabel(toolPart));
            if (StringUtils.hasText(toolPart.getOutputPreview())) {
                state.addTranscriptLine(toolPart.getOutputPreview());
            }
        }
    }

    List<String> renderTranscriptLines(TuiChatState state, int height, int width) {
        if (height <= 0) {
            return List.of();
        }
        List<String> lines = state.transcriptLines().stream()
                .flatMap(line -> line.lines())
                .map(line -> truncate(line, width))
                .toList();
        if (lines.size() <= height) {
            return lines;
        }
        return lines.subList(lines.size() - height, lines.size());
    }

    private GridView createRootView(BoxView transcriptView, InputView promptInput) {
        GridView root = new GridView();
        root.setShowBorder(true);
        root.setTitle(messages.get(CodegeistMessages.TUI_TITLE_KEY));
        root.setRowSize(0, PROMPT_ROW_HEIGHT);
        root.setColumnSize(0);
        root.addItem(transcriptView, 0, 0, 1, 1, 0, 0);
        root.addItem(promptInput, 1, 0, 1, 1, 0, 0);
        return root;
    }

    private BoxView createTranscriptView(TuiChatState state) {
        BoxView transcriptView = new BoxView();
        transcriptView.setShowBorder(true);
        transcriptView.setTitle(messages.get(CodegeistMessages.TUI_TRANSCRIPT_TITLE_KEY));
        transcriptView.setDrawFunction((screen, rect) -> {
            Rectangle innerRect = innerRect(rect);
            List<String> transcriptLines = renderTranscriptLines(state, innerRect.height(), innerRect.width());
            if (transcriptLines.isEmpty()) {
                screen.writerBuilder()
                        .build()
                        .text(messages.get(CodegeistMessages.TUI_EMPTY_TRANSCRIPT_KEY), innerRect,
                                HorizontalAlign.CENTER, VerticalAlign.CENTER);
            }
            else {
                for (int index = 0; index < transcriptLines.size(); index++) {
                    screen.writerBuilder()
                            .build()
                            .text(transcriptLines.get(index), innerRect.x(), innerRect.y() + index);
                }
            }
            return innerRect;
        });
        return transcriptView;
    }

    private InputView createPromptInputView() {
        InputView promptInput = new PromptInputView();
        promptInput.setShowBorder(true);
        promptInput.setTitle(messages.get(CodegeistMessages.TUI_PROMPT_TITLE_KEY));
        return promptInput;
    }

    private String label(String labelKey, String text) {
        return messages.get(labelKey) + LABEL_SEPARATOR + text;
    }

    private String toolLabel(ToolSessionPart part) {
        return label(CodegeistMessages.TUI_TOOL_LABEL_KEY, part.getTool() + SPACE + part.getStatus());
    }

    private String commandFailureMessage(RuntimeException exception) {
        return COMMAND_FAILED_PREFIX + messageOf(exception);
    }

    private boolean isTerminalInterrupt(Throwable throwable) {
        if (throwable instanceof InterruptedIOException) {
            return true;
        }
        Throwable cause = throwable.getCause();
        return cause != null && isTerminalInterrupt(cause);
    }

    private String messageOf(Throwable exception) {
        if (StringUtils.hasText(exception.getMessage())) {
            return exception.getMessage();
        }
        if (exception.getCause() != null) {
            return messageOf(exception.getCause());
        }
        return exception.getClass().getSimpleName();
    }

    private String truncate(String line, int width) {
        if (width <= 0) {
            return "";
        }
        if (line.length() <= width) {
            return line;
        }
        return line.substring(0, width);
    }

    private Rectangle innerRect(Rectangle rect) {
        return new Rectangle(
                rect.x() + 1,
                rect.y() + 1,
                Math.max(0, rect.width() - 2),
                Math.max(0, rect.height() - 2));
    }

    static final class TuiChatState {

        private final List<String> transcriptLines = new ArrayList<>();

        private InputView activePromptInput;

        private TuiChatSurface activeSurface;

        private boolean quitRequested;

        void addTranscriptLine(String line) {
            transcriptLines.add(line);
        }

        List<String> transcriptLines() {
            return transcriptLines;
        }

        InputView activePromptInput() {
            return activePromptInput;
        }

        void activePromptInput(InputView activePromptInput) {
            this.activePromptInput = activePromptInput;
        }

        TuiChatSurface activeSurface() {
            return activeSurface;
        }

        void activeSurface(TuiChatSurface activeSurface) {
            this.activeSurface = activeSurface;
        }

        boolean quitRequested() {
            return quitRequested;
        }

        void quitRequested(boolean quitRequested) {
            this.quitRequested = quitRequested;
        }
    }

    record TuiChatSurface(GridView root, BoxView transcriptView, InputView promptInput) {
    }

    /**
     * Preserves ASCII spaces in prompts while delegating the rest of input editing to
     * Spring Shell's {@link InputView}. The upstream view drops plain spaces because
     * its private insertion path only accepts text with {@code StringUtils.hasText}; a
     * non-breaking-space sentinel survives that path and is normalized before any
     * prompt text leaves this view.
     */
    private static final class PromptInputView extends InputView {

        @Override
        public KeyHandler getKeyHandler() {
            KeyHandler baseHandler = super.getKeyHandler();
            return args -> {
                if (args.event().isKey(Key.Space)) {
                    baseHandler.handle(KeyHandler.argsOf(KeyEvent.of(INPUT_SPACE_SENTINEL)));
                    return KeyHandler.resultOf(args.event(), true, null);
                }
                return baseHandler.handle(args);
            };
        }

        @Override
        public String getInputText() {
            return super.getInputText().replace(INPUT_SPACE_SENTINEL, SPACE);
        }
    }
}
