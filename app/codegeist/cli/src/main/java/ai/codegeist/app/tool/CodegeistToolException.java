package ai.codegeist.app.tool;

final class CodegeistToolException extends RuntimeException {

    CodegeistToolException(String message) {
        super(message);
    }

    CodegeistToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
