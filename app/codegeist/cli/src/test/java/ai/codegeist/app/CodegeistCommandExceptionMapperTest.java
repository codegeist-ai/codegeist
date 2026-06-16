package ai.codegeist.app;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.session.NoSessionToContinueException;
import ai.codegeist.app.session.SessionStoreService;
import org.junit.jupiter.api.Test;
import org.springframework.shell.core.command.ExitStatus;

class CodegeistCommandExceptionMapperTest {

    private final CodegeistCommandExceptionMapper mapper = new CodegeistCommandExceptionMapper();

    @Test
    void mapsNoSessionExceptionToExactUserMessage() {
        ExitStatus status = mapper.apply(new NoSessionToContinueException(
                SessionStoreService.NO_SESSION_TO_CONTINUE_MESSAGE));

        assertThat(status.code()).isEqualTo(ExitStatus.EXECUTION_ERROR.code());
        assertThat(status.description()).isEqualTo(SessionStoreService.NO_SESSION_TO_CONTINUE_MESSAGE);
    }

    @Test
    void unwrapsNoSessionExceptionCause() {
        ExitStatus status = mapper.apply(new IllegalStateException(new NoSessionToContinueException(
                SessionStoreService.NO_SESSION_TO_CONTINUE_MESSAGE)));

        assertThat(status.description()).isEqualTo(SessionStoreService.NO_SESSION_TO_CONTINUE_MESSAGE);
    }

    @Test
    void mapsGenericExceptionToMeaningfulUserMessage() {
        ExitStatus status = mapper.apply(new IllegalStateException("missing provider"));

        assertThat(status.code()).isEqualTo(ExitStatus.EXECUTION_ERROR.code());
        assertThat(status.description()).isEqualTo("Command failed: missing provider");
    }
}
