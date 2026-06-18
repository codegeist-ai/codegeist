package ai.codegeist.app.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionStore {

    public static final int SCHEMA_VERSION = 1;
    public static final String NO_SESSION_TO_CONTINUE_MESSAGE = "No session to continue";

    private int schemaVersion;

    @NonNull
    private String workingDir;

    @NonNull
    private Instant createdAt;

    @NonNull
    private Instant updatedAt;

    @NonNull
    @Builder.Default
    private List<CodegeistSession> sessions = List.of();

    public static SessionStore newStore(@NonNull String workingDir, @NonNull Instant now) {
        return SessionStore.builder()
                .schemaVersion(SCHEMA_VERSION)
                .workingDir(workingDir)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public SessionStore addNewSession(@NonNull Instant now) {
        List<CodegeistSession> updatedSessions = new ArrayList<>(sessions);
        updatedSessions.add(new CodegeistSession(
                UUID.randomUUID(),
                "New session - " + now,
                now,
                now,
                List.of()));
        return copyWith(now, List.copyOf(updatedSessions));
    }

    public SessionStore appendExchangeToLatestSession(
            @NonNull SessionExchange exchange,
            @NonNull Instant userMessageCreatedAt,
            @NonNull Instant completedAt) {
        return appendExchangeToSession(exchange, latestSessionIndex(), userMessageCreatedAt, completedAt);
    }

    public SessionStore appendExchangeToSession(
            @NonNull SessionExchange exchange,
            int sessionIndex,
            @NonNull Instant userMessageCreatedAt,
            @NonNull Instant completedAt) {
        List<CodegeistSession> updatedSessions = new ArrayList<>(sessions);
        CodegeistSession session = updatedSessions.get(sessionIndex);
        UUID userMessageId = UUID.randomUUID();
        SessionMessage userMessage = new SessionMessage(
                userMessageId,
                SessionMessageRole.USER,
                userMessageCreatedAt,
                null,
                null,
                List.of(new TextSessionPart(UUID.randomUUID(), exchange.getPrompt())));
        SessionMessage assistantMessage = new SessionMessage(
                UUID.randomUUID(),
                SessionMessageRole.ASSISTANT,
                completedAt,
                completedAt,
                userMessageId,
                assistantParts(exchange.getResponse(), exchange.getToolParts()));
        List<SessionMessage> updatedMessages = new ArrayList<>(session.messages());
        updatedMessages.add(userMessage);
        updatedMessages.add(assistantMessage);
        updatedSessions.set(sessionIndex, new CodegeistSession(
                session.id(),
                session.title(),
                session.createdAt(),
                completedAt,
                List.copyOf(updatedMessages)));
        return copyWith(completedAt, List.copyOf(updatedSessions));
    }

    public int latestSessionIndex() {
        if (sessions.isEmpty()) {
            throw new NoSessionToContinueException(NO_SESSION_TO_CONTINUE_MESSAGE);
        }

        int latestIndex = 0;
        for (int index = 1; index < sessions.size(); index++) {
            CodegeistSession candidate = sessions.get(index);
            CodegeistSession latest = sessions.get(latestIndex);
            int timestampComparison = candidate.updatedAt().compareTo(latest.updatedAt());
            if (timestampComparison > 0 || timestampComparison == 0 && candidate.id().compareTo(latest.id()) > 0) {
                latestIndex = index;
            }
        }
        return latestIndex;
    }

    private SessionStore copyWith(Instant updatedAt, List<CodegeistSession> sessions) {
        return SessionStore.builder()
                .schemaVersion(schemaVersion)
                .workingDir(workingDir)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .sessions(sessions)
                .build();
    }

    private List<SessionPart> assistantParts(String response, List<ToolSessionPart> toolParts) {
        List<SessionPart> parts = new ArrayList<>();
        List<ToolSessionPart> normalizedToolParts = toolParts == null ? List.of() : toolParts;
        for (ToolSessionPart toolPart : normalizedToolParts) {
            if (toolPart.getId() == null) {
                toolPart.setId(UUID.randomUUID());
            }
            parts.add(toolPart);
        }
        parts.add(new TextSessionPart(UUID.randomUUID(), response));
        return List.copyOf(parts);
    }

    @Getter
    @Setter
    @Builder
    public static class SessionExchange {

        @NonNull
        private String prompt;

        @NonNull
        private String response;

        @NonNull
        @Builder.Default
        private List<ToolSessionPart> toolParts = List.of();
    }
}
