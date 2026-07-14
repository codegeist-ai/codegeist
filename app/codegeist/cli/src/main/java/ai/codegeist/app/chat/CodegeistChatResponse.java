package ai.codegeist.app.chat;

import ai.codegeist.app.session.ToolSessionPart;
import java.util.List;

public record CodegeistChatResponse(String content, List<ToolSessionPart> toolParts) {

    public CodegeistChatResponse(String content) {
        this(content, List.of());
    }
}
