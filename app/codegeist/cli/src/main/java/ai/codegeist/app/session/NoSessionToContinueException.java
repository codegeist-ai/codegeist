package ai.codegeist.app.session;

public class NoSessionToContinueException extends RuntimeException {

    public NoSessionToContinueException(String message) {
        super(message);
    }

    public NoSessionToContinueException(String message, Throwable cause) {
        super(message, cause);
    }
}
