package ai.codegeist.app.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import ai.codegeist.app.config.ProviderConfig;
import java.lang.reflect.RecordComponent;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

class CodegeistChatServiceTest {

    private static final String MODEL = "stub-model";
    private static final String PROMPT = "Hello";
    private static final String RESPONSE = "Hi";

    @Test
    void passesExecutionContextToProviderModel() {
        StubProviderConfig providerConfig = new StubProviderConfig();
        StubChatModel chatModel = new StubChatModel(providerConfig);
        CodegeistChatService service = new StubChatService(chatModel);
        CodegeistChatRequest request = new CodegeistChatRequest(MODEL, PROMPT);
        CodegeistChatExecutionContext context = CodegeistChatExecutionContext.empty(Path.of("."));

        CodegeistChatResponse response = service.chat(providerConfig, request, context);

        assertThat(response.content()).isEqualTo(RESPONSE);
        assertThat(chatModel.request.model()).isEqualTo(MODEL);
        assertThat(chatModel.request.messages()).singleElement().isInstanceOf(UserMessage.class);
        assertThat(((UserMessage) chatModel.request.messages().get(0)).getText()).isEqualTo(PROMPT);
        assertThat(chatModel.context).isSameAs(context);
    }

    @Test
    void rawChatPassesTurnRequestAndContextToProviderModel() {
        StubProviderConfig providerConfig = new StubProviderConfig();
        StubChatModel chatModel = new StubChatModel(providerConfig);
        CodegeistChatService service = new StubChatService(chatModel);
        CodegeistChatTurnRequest request = new CodegeistChatTurnRequest(MODEL, List.of(new UserMessage(PROMPT)));
        CodegeistChatExecutionContext context = CodegeistChatExecutionContext.empty(Path.of("."));

        ChatResponse response = service.rawChat(providerConfig, request, context);

        assertThat(response.getResult().getOutput().getText()).isEqualTo(RESPONSE);
        assertThat(chatModel.request).isSameAs(request);
        assertThat(chatModel.context).isSameAs(context);
    }

    @Test
    void noContextChatOverloadUsesEmptyExecutionContext() {
        StubProviderConfig providerConfig = new StubProviderConfig();
        StubChatModel chatModel = new StubChatModel(providerConfig);
        CodegeistChatService service = new StubChatService(chatModel);
        CodegeistChatRequest request = new CodegeistChatRequest(MODEL, PROMPT);

        CodegeistChatResponse response = service.chat(providerConfig, request);

        assertThat(response.content()).isEqualTo(RESPONSE);
        assertThat(chatModel.request.messages()).singleElement().isInstanceOf(UserMessage.class);
        assertThat(chatModel.context.toolCallbacks()).isEmpty();
    }

    @Test
    void unsupportedProviderTypesFailAtChatServiceBoundary() {
        CodegeistChatService service = new CodegeistChatService();
        StubProviderConfig providerConfig = new StubProviderConfig();
        CodegeistChatRequest request = new CodegeistChatRequest(MODEL, PROMPT);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.chat(providerConfig, request))
                .withMessage(CodegeistChatService.UNSUPPORTED_CHAT_MODEL_MESSAGE_PREFIX + providerConfig.getType());
    }

    @Test
    void chatRequestKeepsOnlyModelAndPromptComponents() {
        assertThat(CodegeistChatRequest.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .containsExactly("model", "prompt");
    }

    private static final class StubProviderConfig extends ProviderConfig {

        @Override
        public String getType() {
            return "stub";
        }

        @Override
        public String defaultModel() {
            return MODEL;
        }
    }

    private static final class StubChatService extends CodegeistChatService {

        private final StubChatModel chatModel;

        private StubChatService(StubChatModel chatModel) {
            this.chatModel = chatModel;
        }

        @Override
        CodegeistChatModel<?> createChatModel(ProviderConfig providerConfig) {
            return chatModel;
        }
    }

    private static final class StubChatModel extends CodegeistChatModel<StubProviderConfig> {

        private CodegeistChatTurnRequest request;
        private CodegeistChatExecutionContext context;

        private StubChatModel(StubProviderConfig providerConfig) {
            super(providerConfig);
        }

        @Override
        public ChatResponse call(CodegeistChatTurnRequest request, CodegeistChatExecutionContext context) {
            this.request = request;
            this.context = context;
            return new ChatResponse(List.of(new Generation(new AssistantMessage(RESPONSE))));
        }
    }
}
