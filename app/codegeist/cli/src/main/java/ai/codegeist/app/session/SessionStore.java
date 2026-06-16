package ai.codegeist.app.session;

import java.time.Instant;
import java.util.List;
import lombok.NonNull;

public record SessionStore(
        int schemaVersion,
        @NonNull String workingDir,
        @NonNull Instant createdAt,
        @NonNull Instant updatedAt,
        @NonNull List<CodegeistSession> sessions) {

    public static final int SCHEMA_VERSION = 1;
}
