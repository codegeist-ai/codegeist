package ai.codegeist.app.chat;

import lombok.NonNull;

public record CodegeistChatRequest(@NonNull String model, @NonNull String prompt) {
}
