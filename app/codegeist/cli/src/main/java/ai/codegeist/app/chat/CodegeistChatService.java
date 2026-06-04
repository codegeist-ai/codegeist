package ai.codegeist.app.chat;

import ai.codegeist.app.config.ProviderConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CodegeistChatService {

    public CodegeistChatResponse chat(@NonNull ProviderConfig providerConfig, @NonNull CodegeistChatRequest request) {
        log.debug("Creating chat model for provider type {}", providerConfig.getType());
        CodegeistChatModel<?> chatModel = providerConfig.createChatModel();
        String content = chatModel.call(request)
                .getResult()
                .getOutput()
                .getText();
        return new CodegeistChatResponse(content);
    }
}
