package ai.codegeist.app.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ToolSessionPart extends SessionPart {

    public static final String TYPE = "tool";
    public static final String TOOL_PROPERTY = "tool";
    public static final String STATUS_PROPERTY = "status";
    public static final String OUTPUT_PREVIEW_PROPERTY = "outputPreview";

    @JsonProperty(TOOL_PROPERTY)
    private String tool;

    @JsonProperty(STATUS_PROPERTY)
    private ToolSessionPartStatus status;

    @JsonProperty(OUTPUT_PREVIEW_PROPERTY)
    private String outputPreview;

    @JsonCreator
    public ToolSessionPart() {
        super(TYPE);
    }

    public enum ToolSessionPartStatus {
        completed,
        failed
    }
}
