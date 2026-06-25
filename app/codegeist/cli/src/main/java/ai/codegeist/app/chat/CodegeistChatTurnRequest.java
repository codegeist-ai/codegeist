package ai.codegeist.app.chat;

import java.util.List;
import lombok.NonNull;
import org.springframework.ai.chat.messages.Message;

/**
 * Internal provider-call request for one model turn inside a Codegeist agent loop.
 *
 * <p>{@link CodegeistChatRequest} remains the command-facing contract with only a
 * runtime model and user prompt. This record carries the Spring AI message history
 * that a provider model needs for a single call, including user messages, assistant
 * tool-call messages, and tool-result messages created by
 * {@link CodegeistAgentLoopService}. The loop owns mutation of the message list and
 * does not mutate it while a synchronous provider call is in progress.
 */
public record CodegeistChatTurnRequest(
        @NonNull String model,
        @NonNull List<Message> messages) {
}
