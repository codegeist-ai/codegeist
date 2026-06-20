package ai.codegeist.app.chat;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.config.ProviderConfig;
import java.lang.reflect.RecordComponent;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

class CodegeistChatServiceTest {

    private static final String MODEL = "stub-model";
    private static final String PROMPT = "Hello";
    private static final String RESPONSE = "Hi";

    @Test
    void passesExecutionContextToProviderModel() {
        CodegeistChatService service = new CodegeistChatService();
        StubProviderConfig providerConfig = new StubProviderConfig();
        CodegeistChatRequest request = new CodegeistChatRequest(MODEL, PROMPT);
        CodegeistChatExecutionContext context = CodegeistChatExecutionContext.empty(Path.of("."));

        CodegeistChatResponse response = service.chat(providerConfig, request, context);

        assertThat(response.content()).isEqualTo(RESPONSE);
        assertThat(providerConfig.chatModel.request).isSameAs(request);
        assertThat(providerConfig.chatModel.context).isSameAs(context);
    }

    @Test
    void noContextChatOverloadUsesEmptyExecutionContext() {
        CodegeistChatService service = new CodegeistChatService();
        StubProviderConfig providerConfig = new StubProviderConfig();
        CodegeistChatRequest request = new CodegeistChatRequest(MODEL, PROMPT);

        CodegeistChatResponse response = service.chat(providerConfig, request);

        assertThat(response.content()).isEqualTo(RESPONSE);
        assertThat(providerConfig.chatModel.request).isSameAs(request);
        assertThat(providerConfig.chatModel.context.toolCallbacks()).isEmpty();
    }

    @Test
    void chatRequestKeepsOnlyModelAndPromptComponents() {
        assertThat(CodegeistChatRequest.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .containsExactly("model", "prompt");
    }

    private static final class StubProviderConfig extends ProviderConfig {

        private final StubChatModel chatModel = new StubChatModel(this);

        @Override
        public String getType() {
            return "stub";
        }

        @Override
        public String defaultModel() {
            return MODEL;
        }

        @Override
        public CodegeistChatModel<?> createChatModel() {
            return chatModel;
        }
    }

    private static final class StubChatModel extends CodegeistChatModel<StubProviderConfig> {

        private CodegeistChatRequest request;
        private CodegeistChatExecutionContext context;

        private StubChatModel(StubProviderConfig providerConfig) {
            super(providerConfig);
        }

        @Override
        public ChatResponse call(CodegeistChatRequest request, CodegeistChatExecutionContext context) {
            this.request = request;
            this.context = context;
            return new ChatResponse(List.of(new Generation(new AssistantMessage(RESPONSE))));
        }
    }
}
