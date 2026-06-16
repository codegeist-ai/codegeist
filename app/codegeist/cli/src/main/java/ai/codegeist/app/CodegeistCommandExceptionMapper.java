package ai.codegeist.app;

import ai.codegeist.app.session.NoSessionToContinueException;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.shell.core.command.ExitStatus;
import org.springframework.shell.core.command.exit.ExitStatusExceptionMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component(CodegeistCommandExceptionMapper.BEAN_NAME)
public class CodegeistCommandExceptionMapper implements ExitStatusExceptionMapper {

    public static final String BEAN_NAME = "codegeistCommandExceptionMapper";

    @Override
    public ExitStatus apply(Exception exception) {
        String userMessage = userMessage(exception);
        log.warn(userMessage, exception);
        return new ExitStatus(ExitStatus.EXECUTION_ERROR.code(), userMessage);
    }

    private String userMessage(Exception exception) {
        NoSessionToContinueException noSessionException = ExceptionUtils.throwableOfType(
                exception,
                NoSessionToContinueException.class);
        if (noSessionException != null) {
            return noSessionException.getMessage();
        }
        return "Command failed: " + messageOf(exception);
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
}
