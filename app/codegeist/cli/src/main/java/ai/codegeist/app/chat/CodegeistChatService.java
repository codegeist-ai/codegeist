package ai.codegeist.app.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class CodegeistChatService {

    @Autowired
    private ChatModelFactory chatModelFactory;

    public CodegeistChatResponse chat(CodegeistChatRequest request) {
        Assert.notNull(request, "Codegeist chat request must not be null");
        Assert.notNull(request.providerConfig(), "Codegeist chat provider config must not be null");
        Assert.hasText(request.prompt(), "Codegeist chat prompt must not be blank");
        log.debug("Creating chat model for provider type {}", request.providerConfig().getType());
        ChatModel chatModel = chatModelFactory.create(request.providerConfig());
        String content = chatModel.call(new Prompt(request.prompt()))
                .getResult()
                .getOutput()
                .getText();
        return new CodegeistChatResponse(content);
    }
}
