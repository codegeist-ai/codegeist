package ai.codegeist.app.session;

import ai.codegeist.app.CodegeistSpringAppProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionStoreService {

    public static final String NO_SESSION_TO_CONTINUE_MESSAGE = "No session to continue";

    private final SessionStoreObjectMapper objectMapper;

    private final SessionStoreClock sessionStoreClock;

    private final CodegeistSpringAppProperties properties;

    @Value("${user.dir}")
    String workingDir;

    public Path currentStorePath() {
        return Path.of(workingDir).toAbsolutePath().normalize()
                .resolve(properties.getSession().getDirectory())
                .resolve(properties.getSession().getStoreFile());
    }

    public SessionStore loadCurrentStoreForContinue() {
        return loadForContinue(currentStorePath());
    }

    public SessionStore appendExchangeToLatestCurrentSession(
            @NonNull SessionStore store,
            @NonNull String prompt,
            @NonNull String response) {
        SessionStore updatedStore = appendExchangeToSession(store, latestSessionIndex(store.sessions()), prompt, response);
        save(currentStorePath(), updatedStore);
        return updatedStore;
    }

    public SessionStore saveExchangeToCurrentSession(
            boolean continueSession,
            @NonNull String prompt,
            @NonNull String response) {
        Path storePath = currentStorePath();
        SessionStore store = loadExistingOrCreateStore(storePath);
        int sessionIndex;
        if (continueSession && !store.sessions().isEmpty()) {
            sessionIndex = latestSessionIndex(store.sessions());
        }
        else {
            store = addNewSession(store);
            sessionIndex = store.sessions().size() - 1;
        }

        SessionStore updatedStore = appendExchangeToSession(store, sessionIndex, prompt, response);
        save(storePath, updatedStore);
        return updatedStore;
    }

    public SessionStore load(@NonNull Path storePath) throws IOException {
        return objectMapper.readValue(storePath.toFile(), SessionStore.class);
    }

    @SneakyThrows(IOException.class)
    public void save(@NonNull Path storePath, @NonNull SessionStore store) {
        Files.createDirectories(storePath.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(storePath.toFile(), store);
    }

    public SessionStore appendExchangeToLatestSession(
            @NonNull SessionStore store,
            @NonNull String prompt,
            @NonNull String response) {
        return appendExchangeToSession(store, latestSessionIndex(store.sessions()), prompt, response);
    }

    private SessionStore appendExchangeToSession(
            SessionStore store,
            int sessionIndex,
            String prompt,
            String response) {
        List<CodegeistSession> sessions = new ArrayList<>(store.sessions());
        CodegeistSession session = sessions.get(sessionIndex);
        Instant now = sessionStoreClock.now();
        UUID userMessageId = UUID.randomUUID();
        SessionMessage userMessage = new SessionMessage(
                userMessageId,
                SessionMessageRole.USER,
                now,
                null,
                null,
                List.of(new TextSessionPart(UUID.randomUUID(), prompt)));
        Instant completedAt = sessionStoreClock.now();
        SessionMessage assistantMessage = new SessionMessage(
                UUID.randomUUID(),
                SessionMessageRole.ASSISTANT,
                completedAt,
                completedAt,
                userMessageId,
                List.of(new TextSessionPart(UUID.randomUUID(), response)));
        List<SessionMessage> updatedMessages = new ArrayList<>(session.messages());
        updatedMessages.add(userMessage);
        updatedMessages.add(assistantMessage);
        CodegeistSession updatedSession = new CodegeistSession(
                session.id(),
                session.title(),
                session.createdAt(),
                completedAt,
                List.copyOf(updatedMessages));
        sessions.set(sessionIndex, updatedSession);
        return new SessionStore(
                store.schemaVersion(),
                store.workingDir(),
                store.createdAt(),
                completedAt,
                List.copyOf(sessions));
    }

    private SessionStore loadExistingOrCreateStore(Path storePath) {
        if (!Files.exists(storePath)) {
            return newStore();
        }

        try {
            SessionStore store = load(storePath);
            validateLoadableStore(store);
            return store;
        }
        catch (IOException exception) {
            throw new NoSessionToContinueException(NO_SESSION_TO_CONTINUE_MESSAGE, exception);
        }
    }

    private SessionStore newStore() {
        Instant now = sessionStoreClock.now();
        return new SessionStore(
                SessionStore.SCHEMA_VERSION,
                Path.of(workingDir).toAbsolutePath().normalize().toString(),
                now,
                now,
                List.of());
    }

    private SessionStore addNewSession(SessionStore store) {
        Instant now = sessionStoreClock.now();
        List<CodegeistSession> sessions = new ArrayList<>(store.sessions());
        sessions.add(new CodegeistSession(
                UUID.randomUUID(),
                "New session - " + now,
                now,
                now,
                List.of()));
        return new SessionStore(
                store.schemaVersion(),
                store.workingDir(),
                store.createdAt(),
                now,
                List.copyOf(sessions));
    }

    private SessionStore loadForContinue(Path storePath) {
        if (!Files.exists(storePath)) {
            throw new NoSessionToContinueException(NO_SESSION_TO_CONTINUE_MESSAGE);
        }

        try {
            SessionStore store = load(storePath);
            validateContinuationStore(store);
            return store;
        }
        catch (IOException exception) {
            throw new NoSessionToContinueException(NO_SESSION_TO_CONTINUE_MESSAGE, exception);
        }
    }

    private void validateContinuationStore(SessionStore store) {
        if (store == null
                || store.schemaVersion() != SessionStore.SCHEMA_VERSION
                || store.sessions() == null
                || store.sessions().isEmpty()) {
            throw new NoSessionToContinueException(NO_SESSION_TO_CONTINUE_MESSAGE);
        }
    }

    private void validateLoadableStore(SessionStore store) {
        if (store == null
                || store.schemaVersion() != SessionStore.SCHEMA_VERSION
                || store.sessions() == null) {
            throw new NoSessionToContinueException(NO_SESSION_TO_CONTINUE_MESSAGE);
        }
    }

    private int latestSessionIndex(List<CodegeistSession> sessions) {
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
}
