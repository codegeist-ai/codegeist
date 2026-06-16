package ai.codegeist.app.session;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;

public record SessionMessage(
        @NonNull UUID id,
        @NonNull SessionMessageRole role,
        @NonNull Instant createdAt,
        Instant completedAt,
        UUID parentMessageId,
        @NonNull List<SessionPart> parts) {
}
