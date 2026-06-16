package ai.codegeist.app.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum SessionMessageRole {
    USER("user"),
    ASSISTANT("assistant");

    private final String value;

    SessionMessageRole(String value) {
        this.value = value;
    }

    @JsonCreator
    public static SessionMessageRole fromValue(String value) {
        return Arrays.stream(values())
                .filter(role -> role.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported session message role: " + value));
    }

    @JsonValue
    public String value() {
        return value;
    }
}
