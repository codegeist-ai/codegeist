package ai.codegeist.app.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import ai.codegeist.app.config.ProviderConfig;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

class CodegeistAgentLoopServiceTest {

    private static final String MODEL = "stub-model";
    private static final String PROMPT = "Read README";
    private static final String TOOL_CALL_ID = "call-1";
    private static final String TOOL_CALL_TYPE = "function";
    private static final String TOOL_NAME = "fake_tool";
    private static final String TOOL_ARGUMENTS = "{\"path\":\"README.md\"}";
    private static final String TOOL_OUTPUT = "README contents";
    private static final String SECOND_TOOL_CALL_ID = "call-2";
    private static final String SECOND_TOOL_NAME = "fake_shell";
    private static final String SECOND_TOOL_ARGUMENTS = "{\"command\":\"sh hello-world.sh\"}";
    private static final String SECOND_TOOL_OUTPUT = "Hello World";
    private static final String FINAL_RESPONSE = "The README was read.";

    @Test
    void runsSecondModelTurnAfterToolCallAndFeedsToolResultToContinuation() {
        FakeToolCallback callback = new FakeToolCallback(TOOL_NAME, TOOL_OUTPUT);
        StubProviderConfig providerConfig = new StubProviderConfig(
                request -> toolCallResponse(TOOL_CALL_ID, TOOL_NAME, TOOL_ARGUMENTS),
                request -> {
                    assertContinuationMessages(request, TOOL_NAME, TOOL_ARGUMENTS, TOOL_OUTPUT);
                    return finalResponse(FINAL_RESPONSE);
                });
        CodegeistAgentLoopService service = service(providerConfig);

        CodegeistChatResponse response = service.run(
                providerConfig,
                new CodegeistChatRequest(MODEL, PROMPT),
                context(callback));

        assertThat(response.content()).isEqualTo(FINAL_RESPONSE);
        assertThat(providerConfig.chatModel.requests).hasSize(2);
        assertThat(callback.inputs).containsExactly(TOOL_ARGUMENTS);
    }

    @Test
    void dispatchesMultipleToolCallsInOrderAndFeedsTheirResultsToContinuation() {
        List<String> invocationOrder = new ArrayList<>();
        FakeToolCallback firstCallback = new FakeToolCallback(TOOL_NAME, TOOL_OUTPUT, invocationOrder);
        FakeToolCallback secondCallback = new FakeToolCallback(SECOND_TOOL_NAME, SECOND_TOOL_OUTPUT, invocationOrder);
        StubProviderConfig providerConfig = new StubProviderConfig(
                request -> toolCallResponse(List.of(
                        new AssistantMessage.ToolCall(
                                TOOL_CALL_ID, TOOL_CALL_TYPE, TOOL_NAME, TOOL_ARGUMENTS),
                        new AssistantMessage.ToolCall(
                                SECOND_TOOL_CALL_ID, TOOL_CALL_TYPE, SECOND_TOOL_NAME, SECOND_TOOL_ARGUMENTS))),
                request -> {
                    assertThat(request.messages()).hasSize(3);
                    AssistantMessage assistantMessage = (AssistantMessage) request.messages().get(1);
                    assertThat(assistantMessage.getToolCalls())
                            .extracting(AssistantMessage.ToolCall::name)
                            .containsExactly(TOOL_NAME, SECOND_TOOL_NAME);
                    ToolResponseMessage toolResponseMessage = (ToolResponseMessage) request.messages().get(2);
                    assertThat(toolResponseMessage.getResponses())
                            .extracting(ToolResponseMessage.ToolResponse::name)
                            .containsExactly(TOOL_NAME, SECOND_TOOL_NAME);
                    assertThat(toolResponseMessage.getResponses())
                            .extracting(ToolResponseMessage.ToolResponse::responseData)
                            .containsExactly(TOOL_OUTPUT, SECOND_TOOL_OUTPUT);
                    return finalResponse(FINAL_RESPONSE);
                });
        CodegeistAgentLoopService service = service(providerConfig);

        CodegeistChatResponse response = service.run(
                providerConfig,
                new CodegeistChatRequest(MODEL, PROMPT),
                new CodegeistChatExecutionContext(Path.of("."), List.of(firstCallback, secondCallback)));

        assertThat(response.content()).isEqualTo(FINAL_RESPONSE);
        assertThat(invocationOrder).containsExactly(TOOL_NAME, SECOND_TOOL_NAME);
        assertThat(firstCallback.inputs).containsExactly(TOOL_ARGUMENTS);
        assertThat(secondCallback.inputs).containsExactly(SECOND_TOOL_ARGUMENTS);
    }

    @Test
    void returnsMissingToolResultToModelContinuation() {
        String missingTool = "missing_tool";
        StubProviderConfig providerConfig = new StubProviderConfig(
                request -> toolCallResponse(TOOL_CALL_ID, missingTool, TOOL_ARGUMENTS),
                request -> {
                    ToolResponseMessage toolResponseMessage = (ToolResponseMessage) request.messages().get(2);
                    assertThat(toolResponseMessage.getResponses()).singleElement().satisfies(response -> {
                        assertThat(response.name()).isEqualTo(missingTool);
                        assertThat(response.responseData()).isEqualTo(
                                CodegeistAgentLoopService.UNKNOWN_TOOL_MESSAGE_PREFIX + missingTool);
                    });
                    return finalResponse(FINAL_RESPONSE);
                });
        CodegeistAgentLoopService service = service(providerConfig);

        CodegeistChatResponse response = service.run(
                providerConfig,
                new CodegeistChatRequest(MODEL, PROMPT),
                CodegeistChatExecutionContext.empty(Path.of(".")));

        assertThat(response.content()).isEqualTo(FINAL_RESPONSE);
        assertThat(providerConfig.chatModel.requests).hasSize(2);
    }

    @Test
    void stopsAtMaxToolRounds() {
        FakeToolCallback callback = new FakeToolCallback(TOOL_NAME, TOOL_OUTPUT);
        StubProviderConfig providerConfig = new StubProviderConfig(
                request -> toolCallResponse(TOOL_CALL_ID, TOOL_NAME, TOOL_ARGUMENTS));
        CodegeistAgentLoopService service = service(providerConfig);

        assertThatIllegalStateException()
                .isThrownBy(() -> service.run(
                        providerConfig,
                        new CodegeistChatRequest(MODEL, PROMPT),
                        context(callback)))
                .withMessage(CodegeistAgentLoopService.MAX_TOOL_ROUNDS_MESSAGE);
        assertThat(providerConfig.chatModel.requests).hasSize(CodegeistAgentLoopService.MAX_TOOL_ROUNDS + 1);
        assertThat(callback.inputs).hasSize(CodegeistAgentLoopService.MAX_TOOL_ROUNDS);
    }

    @Test
    void rejectsDuplicateToolCallbackNamesBeforeCallingModel() {
        StubProviderConfig providerConfig = new StubProviderConfig(
                request -> finalResponse(FINAL_RESPONSE));
        CodegeistAgentLoopService service = service(providerConfig);

        assertThatIllegalStateException()
                .isThrownBy(() -> service.run(
                        providerConfig,
                        new CodegeistChatRequest(MODEL, PROMPT),
                        new CodegeistChatExecutionContext(Path.of("."), List.of(
                                new FakeToolCallback(TOOL_NAME, "first"),
                                new FakeToolCallback(TOOL_NAME, "second")))))
                .withMessage(CodegeistAgentLoopService.DUPLICATE_TOOL_MESSAGE_PREFIX + TOOL_NAME);
        assertThat(providerConfig.chatModel.requests).isEmpty();
    }

    private static void assertContinuationMessages(
            CodegeistChatTurnRequest request,
            String toolName,
            String toolArguments,
            String toolOutput) {
        assertThat(request.model()).isEqualTo(MODEL);
        assertThat(request.messages()).hasSize(3);
        assertThat(request.messages().get(0)).isInstanceOf(UserMessage.class);
        assertThat(((UserMessage) request.messages().get(0)).getText()).isEqualTo(PROMPT);

        assertThat(request.messages().get(1)).isInstanceOf(AssistantMessage.class);
        AssistantMessage assistantMessage = (AssistantMessage) request.messages().get(1);
        assertThat(assistantMessage.getToolCalls()).singleElement().satisfies(toolCall -> {
            assertThat(toolCall.id()).isEqualTo(TOOL_CALL_ID);
            assertThat(toolCall.name()).isEqualTo(toolName);
            assertThat(toolCall.arguments()).isEqualTo(toolArguments);
        });

        assertThat(request.messages().get(2)).isInstanceOf(ToolResponseMessage.class);
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) request.messages().get(2);
        assertThat(toolResponseMessage.getResponses()).singleElement().satisfies(response -> {
            assertThat(response.id()).isEqualTo(TOOL_CALL_ID);
            assertThat(response.name()).isEqualTo(toolName);
            assertThat(response.responseData()).isEqualTo(toolOutput);
        });
    }

    private static CodegeistChatExecutionContext context(ToolCallback callback) {
        return new CodegeistChatExecutionContext(Path.of("."), List.of(callback));
    }

    private static CodegeistAgentLoopService service(StubProviderConfig providerConfig) {
        return new CodegeistAgentLoopService(new StubChatService(providerConfig.chatModel));
    }

    private static ChatResponse toolCallResponse(String id, String name, String arguments) {
        return toolCallResponse(List.of(new AssistantMessage.ToolCall(id, TOOL_CALL_TYPE, name, arguments)));
    }

    private static ChatResponse toolCallResponse(List<AssistantMessage.ToolCall> toolCalls) {
        return new ChatResponse(List.of(new Generation(AssistantMessage.builder()
                .content("")
                .toolCalls(toolCalls)
                .build())));
    }

    private static ChatResponse finalResponse(String content) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(content))));
    }

    private static final class StubProviderConfig extends ProviderConfig {

        private final ScriptedChatModel chatModel;

        @SafeVarargs
        private StubProviderConfig(Function<CodegeistChatTurnRequest, ChatResponse>... turns) {
            chatModel = new ScriptedChatModel(this, List.of(turns));
        }

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

        private final ScriptedChatModel chatModel;

        private StubChatService(ScriptedChatModel chatModel) {
            this.chatModel = chatModel;
        }

        @Override
        CodegeistChatModel<?> createChatModel(ProviderConfig providerConfig) {
            return chatModel;
        }
    }

    private static final class ScriptedChatModel extends CodegeistChatModel<StubProviderConfig> {

        private final List<Function<CodegeistChatTurnRequest, ChatResponse>> turns;
        private final List<CodegeistChatTurnRequest> requests = new ArrayList<>();
        private int nextTurn;

        private ScriptedChatModel(
                StubProviderConfig providerConfig,
                List<Function<CodegeistChatTurnRequest, ChatResponse>> turns) {
            super(providerConfig);
            this.turns = turns;
        }

        @Override
        public ChatResponse call(CodegeistChatTurnRequest request, CodegeistChatExecutionContext context) {
            requests.add(request);
            Function<CodegeistChatTurnRequest, ChatResponse> turn = turns.get(Math.min(nextTurn, turns.size() - 1));
            nextTurn++;
            return turn.apply(request);
        }
    }

    private static final class FakeToolCallback implements ToolCallback {

        private final String name;
        private final String output;
        private final List<String> inputs = new ArrayList<>();
        private final List<String> invocationOrder;

        private FakeToolCallback(String name, String output) {
            this(name, output, new ArrayList<>());
        }

        private FakeToolCallback(String name, String output, List<String> invocationOrder) {
            this.name = name;
            this.output = output;
            this.invocationOrder = invocationOrder;
        }

        @Override
        public ToolDefinition getToolDefinition() {
            return ToolDefinition.builder()
                    .name(name)
                    .description("Fake tool")
                    .inputSchema("{\"type\":\"object\"}")
                    .build();
        }

        @Override
        public ToolMetadata getToolMetadata() {
            return ToolMetadata.builder().returnDirect(false).build();
        }

        @Override
        public String call(String toolInput) {
            inputs.add(toolInput);
            invocationOrder.add(name);
            return output;
        }

        @Override
        public String call(String toolInput, ToolContext toolContext) {
            return call(toolInput);
        }
    }
}
