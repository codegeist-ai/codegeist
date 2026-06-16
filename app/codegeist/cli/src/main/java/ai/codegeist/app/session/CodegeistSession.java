package ai.codegeist.app.session;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;

public record CodegeistSession(
        @NonNull UUID id,
        @NonNull String title,
        @NonNull Instant createdAt,
        @NonNull Instant updatedAt,
        @NonNull List<SessionMessage> messages) {
}
