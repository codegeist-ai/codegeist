package ai.codegeist.app.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CompactionSessionPart extends SessionPart {

    public static final String TYPE = "compaction";
    public static final String AUTO_PROPERTY = "auto";
    public static final String OVERFLOW_PROPERTY = "overflow";
    public static final String TAIL_START_MESSAGE_ID_PROPERTY = "tailStartMessageId";

    @JsonProperty(AUTO_PROPERTY)
    private boolean auto;

    @JsonProperty(OVERFLOW_PROPERTY)
    private boolean overflow;

    @JsonProperty(TAIL_START_MESSAGE_ID_PROPERTY)
    private UUID tailStartMessageId;

    @JsonCreator
    public CompactionSessionPart() {
        super(TYPE);
    }
}
