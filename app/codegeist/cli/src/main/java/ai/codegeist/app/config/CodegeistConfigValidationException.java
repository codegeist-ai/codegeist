package ai.codegeist.app.config;

public class CodegeistConfigValidationException extends IllegalArgumentException {

    public CodegeistConfigValidationException(String message) {
        super(message);
    }

    public CodegeistConfigValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
