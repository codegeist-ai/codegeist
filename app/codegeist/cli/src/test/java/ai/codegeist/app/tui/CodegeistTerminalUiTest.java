package ai.codegeist.app.tui;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistSpringAppProperties;
import ai.codegeist.app.chat.ChatHarnessService;
import ai.codegeist.app.chat.CodegeistChatResponse;
import ai.codegeist.app.i18n.CodegeistLocaleService;
import ai.codegeist.app.i18n.CodegeistMessages;
import ai.codegeist.app.session.ToolSessionPart;
import ai.codegeist.app.session.ToolSessionPart.ToolSessionPartStatus;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.IOError;
import java.io.InterruptedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import org.junit.jupiter.api.Test;
import org.jline.terminal.impl.DumbTerminal;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.shell.jline.tui.component.view.TerminalUI;
import org.springframework.shell.jline.tui.component.view.control.InputView;
import org.springframework.shell.jline.tui.component.view.event.KeyEvent;
import org.springframework.shell.jline.tui.component.view.event.KeyHandler;

class CodegeistTerminalUiTest {

    @Test
    void installsChatSurfaceWithFocusedPrompt() throws IOException {
        CodegeistTerminalUi terminalUi = new CodegeistTerminalUi(null, messages(), new StubChatHarnessService());
        TerminalUI springTerminalUi = terminalUi();
        CodegeistTerminalUi.TuiChatState state = new CodegeistTerminalUi.TuiChatState();

        CodegeistTerminalUi.TuiChatSurface surface = terminalUi.installChatSurface(springTerminalUi, state);

        assertThat(surface.root().isShowBorder()).isTrue();
        assertThat(surface.transcriptView().isShowBorder()).isTrue();
        assertThat(surface.transcriptView().getDrawFunction()).isNotNull();
        assertThat(surface.promptInput().isShowBorder()).isTrue();
        assertThat(surface.promptInput()).isSameAs(state.activePromptInput());
        assertThat(surface.promptInput().hasFocus()).isTrue();
    }

    @Test
    void submitsPromptThroughHarnessAndDisplaysResponse() throws IOException {
        StubChatHarnessService chatHarnessService = new StubChatHarnessService();
        chatHarnessService.respondWith("Hello from Codegeist");
        CodegeistTerminalUi terminalUi = new CodegeistTerminalUi(null, messages(), chatHarnessService);
        TerminalUI springTerminalUi = terminalUi();
        CodegeistTerminalUi.TuiChatState state = new CodegeistTerminalUi.TuiChatState();
        terminalUi.installChatSurface(springTerminalUi, state);

        terminalUi.submitPrompt(springTerminalUi, state, "Hello");

        assertThat(chatHarnessService.continueSession).isTrue();
        assertThat(chatHarnessService.prompts).containsExactly("Hello");
        assertThat(terminalUi.renderTranscriptLines(state, 10, 80))
                .containsExactly("You: Hello", "Codegeist: Hello from Codegeist");
        assertThat(state.activePromptInput().hasFocus()).isTrue();
    }

    @Test
    void typedCharactersAndSpacesReachFocusedPromptInputThroughRootHandler() throws IOException {
        CodegeistTerminalUi terminalUi = new CodegeistTerminalUi(null, messages(), new StubChatHarnessService());
        TerminalUI springTerminalUi = terminalUi();
        CodegeistTerminalUi.TuiChatState state = new CodegeistTerminalUi.TuiChatState();
        CodegeistTerminalUi.TuiChatSurface surface = terminalUi.installChatSurface(springTerminalUi, state);

        surface.root().getKeyHandler().handle(KeyHandler.argsOf(KeyEvent.of('h')));
        surface.root().getKeyHandler().handle(KeyHandler.argsOf(KeyEvent.of(' ')));
        surface.root().getKeyHandler().handle(KeyHandler.argsOf(KeyEvent.of('i')));

        assertThat(surface.promptInput().getInputText()).isEqualTo("h i");
    }

    @Test
    void displaysToolPartsBeforeAssistantResponse() throws IOException {
        StubChatHarnessService chatHarnessService = new StubChatHarnessService();
        chatHarnessService.respondWith(new CodegeistChatResponse(
                "Done",
                List.of(
                        toolPart("codegeist_write", "Created file: hello-world.sh\nCharacters: 19"),
                        toolPart(
                                "codegeist_shell",
                                "Command: sh hello-world.sh\nExit code: 0\nOutput:\nHello World\n"))));
        CodegeistTerminalUi terminalUi = new CodegeistTerminalUi(null, messages(), chatHarnessService);
        TerminalUI springTerminalUi = terminalUi();
        CodegeistTerminalUi.TuiChatState state = new CodegeistTerminalUi.TuiChatState();
        terminalUi.installChatSurface(springTerminalUi, state);

        terminalUi.submitPrompt(springTerminalUi, state, "Run hello world");

        assertThat(terminalUi.renderTranscriptLines(state, 20, 120))
                .containsExactly(
                        "You: Run hello world",
                        "Tool: codegeist_write completed",
                        "Created file: hello-world.sh",
                        "Characters: 19",
                        "Tool: codegeist_shell completed",
                        "Command: sh hello-world.sh",
                        "Exit code: 0",
                        "Output:",
                        "Hello World",
                        "Codegeist: Done");
    }

    @Test
    void resetsFocusedPromptInputAfterSubmission() throws IOException {
        StubChatHarnessService chatHarnessService = new StubChatHarnessService();
        chatHarnessService.respondWith("Hello from Codegeist");
        CodegeistTerminalUi terminalUi = new CodegeistTerminalUi(null, messages(), chatHarnessService);
        TerminalUI springTerminalUi = terminalUi();
        CodegeistTerminalUi.TuiChatState state = new CodegeistTerminalUi.TuiChatState();
        CodegeistTerminalUi.TuiChatSurface surface = terminalUi.installChatSurface(springTerminalUi, state);
        surface.root().getKeyHandler().handle(KeyHandler.argsOf(KeyEvent.of('h')));
        surface.root().getKeyHandler().handle(KeyHandler.argsOf(KeyEvent.of('i')));
        InputView submittedPromptInput = state.activePromptInput();

        terminalUi.submitPrompt(springTerminalUi, state, submittedPromptInput.getInputText());

        assertThat(state.activePromptInput()).isNotSameAs(submittedPromptInput);
        assertThat(state.activePromptInput().getInputText()).isEmpty();
        assertThat(state.activePromptInput().hasFocus()).isTrue();
        surface.root().getKeyHandler().handle(KeyHandler.argsOf(KeyEvent.of('n')));
        assertThat(state.activePromptInput().getInputText()).isEqualTo("n");
    }

    @Test
    void supportsRepeatedPromptSubmissions() throws IOException {
        StubChatHarnessService chatHarnessService = new StubChatHarnessService();
        chatHarnessService.respondWith("First response");
        chatHarnessService.respondWith("Second response");
        CodegeistTerminalUi terminalUi = new CodegeistTerminalUi(null, messages(), chatHarnessService);
        TerminalUI springTerminalUi = terminalUi();
        CodegeistTerminalUi.TuiChatState state = new CodegeistTerminalUi.TuiChatState();
        terminalUi.installChatSurface(springTerminalUi, state);

        terminalUi.submitPrompt(springTerminalUi, state, "First prompt");
        terminalUi.submitPrompt(springTerminalUi, state, "Second prompt");

        assertThat(chatHarnessService.prompts).containsExactly("First prompt", "Second prompt");
        assertThat(terminalUi.renderTranscriptLines(state, 10, 80))
                .containsExactly(
                        "You: First prompt",
                        "Codegeist: First response",
                        "You: Second prompt",
                        "Codegeist: Second response");
        assertThat(state.activePromptInput().hasFocus()).isTrue();
    }

    @Test
    void displaysHandledHarnessFailureAndKeepsPromptUsable() throws IOException {
        StubChatHarnessService chatHarnessService = new StubChatHarnessService();
        chatHarnessService.failWith(new IllegalStateException("No provider configured"));
        CodegeistTerminalUi terminalUi = new CodegeistTerminalUi(null, messages(), chatHarnessService);
        TerminalUI springTerminalUi = terminalUi();
        CodegeistTerminalUi.TuiChatState state = new CodegeistTerminalUi.TuiChatState();
        terminalUi.installChatSurface(springTerminalUi, state);

        terminalUi.submitPrompt(springTerminalUi, state, "Hello");

        assertThat(chatHarnessService.prompts).containsExactly("Hello");
        assertThat(terminalUi.renderTranscriptLines(state, 10, 80))
                .containsExactly("You: Hello", "Error: Command failed: No provider configured");
        assertThat(state.activePromptInput().hasFocus()).isTrue();
    }

    @Test
    void ignoresBlankPromptWithoutCallingHarness() throws IOException {
        StubChatHarnessService chatHarnessService = new StubChatHarnessService();
        CodegeistTerminalUi terminalUi = new CodegeistTerminalUi(null, messages(), chatHarnessService);
        TerminalUI springTerminalUi = terminalUi();
        CodegeistTerminalUi.TuiChatState state = new CodegeistTerminalUi.TuiChatState();
        terminalUi.installChatSurface(springTerminalUi, state);

        terminalUi.submitPrompt(springTerminalUi, state, "   ");

        assertThat(chatHarnessService.prompts).isEmpty();
        assertThat(terminalUi.renderTranscriptLines(state, 10, 80)).isEmpty();
        assertThat(state.activePromptInput().hasFocus()).isTrue();
    }

    @Test
    void rendersVisibleTranscriptWindow() {
        CodegeistTerminalUi terminalUi = new CodegeistTerminalUi(null, messages(), new StubChatHarnessService());
        CodegeistTerminalUi.TuiChatState state = new CodegeistTerminalUi.TuiChatState();
        state.addTranscriptLine("first line");
        state.addTranscriptLine("second line");
        state.addTranscriptLine("third line");

        assertThat(terminalUi.renderTranscriptLines(state, 2, 6))
                .containsExactly("second", "third ");
    }

    @Test
    void terminalInterruptAfterPromptCompletionDoesNotRequestQuit() {
        CodegeistTerminalUi terminalUi = new CodegeistTerminalUi(null, messages(), new StubChatHarnessService());
        CodegeistTerminalUi.TuiChatState state = new CodegeistTerminalUi.TuiChatState();

        terminalUi.handleTerminalRunError(state, new IOError(new InterruptedIOException("input interrupted")));

        assertThat(state.quitRequested()).isFalse();
    }

    private static TerminalUI terminalUi() throws IOException {
        return new TerminalUI(new DumbTerminal(
                new ByteArrayInputStream(new byte[0]),
                new ByteArrayOutputStream()));
    }

    private static CodegeistMessages messages() {
        StaticMessageSource messageSource = new StaticMessageSource();
        CodegeistLocaleService localeService = new CodegeistLocaleService(new CodegeistSpringAppProperties());
        Locale locale = localeService.currentLocale();
        messageSource.addMessage(CodegeistMessages.TUI_TITLE_KEY, locale, "Codegeist");
        messageSource.addMessage(CodegeistMessages.TUI_TRANSCRIPT_TITLE_KEY, locale, "Transcript");
        messageSource.addMessage(CodegeistMessages.TUI_PROMPT_TITLE_KEY, locale, "Prompt");
        messageSource.addMessage(CodegeistMessages.TUI_EMPTY_TRANSCRIPT_KEY, locale,
                "Enter a prompt below. Press Ctrl-Q to quit.");
        messageSource.addMessage(CodegeistMessages.TUI_USER_LABEL_KEY, locale, "You");
        messageSource.addMessage(CodegeistMessages.TUI_ASSISTANT_LABEL_KEY, locale, "Codegeist");
        messageSource.addMessage(CodegeistMessages.TUI_ERROR_LABEL_KEY, locale, "Error");
        messageSource.addMessage(CodegeistMessages.TUI_TOOL_LABEL_KEY, locale, "Tool");
        return new CodegeistMessages(messageSource, localeService);
    }

    private static ToolSessionPart toolPart(String toolName, String outputPreview) {
        ToolSessionPart part = new ToolSessionPart();
        part.setTool(toolName);
        part.setStatus(ToolSessionPartStatus.completed);
        part.setOutputPreview(outputPreview);
        return part;
    }

    private static final class StubChatHarnessService extends ChatHarnessService {

        private final Queue<Object> outcomes = new ArrayDeque<>();
        private final List<String> prompts = new ArrayList<>();
        private boolean continueSession;

        private StubChatHarnessService() {
            super(null, null, null, null, null);
        }

        private void respondWith(String response) {
            respondWith(new CodegeistChatResponse(response));
        }

        private void respondWith(CodegeistChatResponse response) {
            outcomes.add(response);
        }

        private void failWith(RuntimeException exception) {
            outcomes.add(exception);
        }

        @Override
        public CodegeistChatResponse ask(boolean continueSession, String prompt) {
            this.continueSession = continueSession;
            prompts.add(prompt);
            Object outcome = outcomes.remove();
            if (outcome instanceof RuntimeException exception) {
                throw exception;
            }
            return (CodegeistChatResponse) outcome;
        }
    }
}
