package ai.codegeist.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TuiCommandsTest {

    @Test
    void tuiCommandStartsTerminalUi() {
        RecordingTerminalUi terminalUi = new RecordingTerminalUi();
        TuiCommands commands = new TuiCommands(terminalUi);

        commands.tui();

        assertThat(terminalUi.runCount).isEqualTo(1);
    }

    private static final class RecordingTerminalUi extends CodegeistTerminalUi {

        private int runCount;

        private RecordingTerminalUi() {
            super(null);
        }

        @Override
        void run() {
            runCount++;
        }
    }
}
