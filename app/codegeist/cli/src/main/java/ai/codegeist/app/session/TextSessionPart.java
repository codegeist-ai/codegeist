package ai.codegeist.app.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class TextSessionPart extends SessionPart {

    public static final String TYPE = "text";
    public static final String TEXT_PROPERTY = "text";

    @JsonProperty(TEXT_PROPERTY)
    private String text;

    @JsonCreator
    public TextSessionPart() {
        super(TYPE);
    }

    public TextSessionPart(@NonNull UUID id, @NonNull String text) {
        this();
        setId(id);
        this.text = text;
    }
}
