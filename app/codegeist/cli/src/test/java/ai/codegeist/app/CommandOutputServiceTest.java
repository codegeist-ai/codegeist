package ai.codegeist.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.ParsedInput;

class CommandOutputServiceTest {

    @Test
    void printWritesAndFlushesCommandOutputWriter() {
        CommandOutputService outputService = new CommandOutputService();
        FlushTrackingWriter output = new FlushTrackingWriter();

        outputService.print(commandContext(output), "hello");

        assertThat(output).hasToString("hello");
        assertThat(output.flushed).isTrue();
    }

    private CommandContext commandContext(StringWriter output) {
        return new CommandContext(
                ParsedInput.builder().commandName("test-command").build(),
                null,
                new PrintWriter(output),
                null);
    }

    private static final class FlushTrackingWriter extends StringWriter {

        private boolean flushed;

        @Override
        public void flush() {
            flushed = true;
            super.flush();
        }
    }
}
