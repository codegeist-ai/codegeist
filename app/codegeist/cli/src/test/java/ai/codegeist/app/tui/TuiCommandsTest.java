package ai.codegeist.app.tui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TuiCommandsTest {

    @Test
    void delegatesToTerminalUi() {
        FakeTerminalUi terminalUi = new FakeTerminalUi();
        TuiCommands commands = new TuiCommands(terminalUi);

        commands.tui();

        assertThat(terminalUi.ran).isTrue();
    }

    private static class FakeTerminalUi extends CodegeistTerminalUi {

        private boolean ran;

        FakeTerminalUi() {
            super(null, null, null);
        }

        @Override
        void run() {
            ran = true;
        }
    }
}
