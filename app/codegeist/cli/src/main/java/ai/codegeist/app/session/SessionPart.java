package ai.codegeist.app.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = SessionPart.TYPE_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextSessionPart.class, name = TextSessionPart.TYPE),
        @JsonSubTypes.Type(value = CompactionSessionPart.class, name = CompactionSessionPart.TYPE)
})
@Getter
public abstract sealed class SessionPart permits TextSessionPart, CompactionSessionPart {

    public static final String TYPE_PROPERTY = "type";
    public static final String ID_PROPERTY = "id";

    @JsonProperty(ID_PROPERTY)
    @Setter
    private UUID id;

    @JsonIgnore
    private final String type;

    protected SessionPart(@NonNull String type) {
        this.type = type;
    }
}
