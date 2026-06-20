package ai.codegeist.app.chat;

import ai.codegeist.app.config.ProviderConfig;
import java.nio.file.Path;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CodegeistChatService {

    public CodegeistChatResponse chat(@NonNull ProviderConfig providerConfig, @NonNull CodegeistChatRequest request) {
        return chat(providerConfig, request, CodegeistChatExecutionContext.empty(Path.of(".")));
    }

    public CodegeistChatResponse chat(
            @NonNull ProviderConfig providerConfig,
            @NonNull CodegeistChatRequest request,
            @NonNull CodegeistChatExecutionContext context) {
        log.debug(
                "Creating chat model for provider type {} with {} tool callbacks",
                providerConfig.getType(),
                context.toolCallbacks().size());
        CodegeistChatModel<?> chatModel = providerConfig.createChatModel();
        return response(chatModel.call(request, context));
    }

    private CodegeistChatResponse response(ChatResponse chatResponse) {
        String content = chatResponse
                .getResult()
                .getOutput()
                .getText();
        return new CodegeistChatResponse(content);
    }
}
