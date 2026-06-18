package ai.codegeist.app.session;

import ai.codegeist.app.CodegeistSpringAppProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionStoreService {

    public static final String NO_SESSION_TO_CONTINUE_MESSAGE = SessionStore.NO_SESSION_TO_CONTINUE_MESSAGE;

    private final SessionStoreObjectMapper objectMapper;

    private final SessionStoreClock sessionStoreClock;

    private final CodegeistSpringAppProperties properties;

    @Value("${user.dir}")
    String workingDir;

    public Path currentWorkingDirectory() {
        return Path.of(workingDir).toAbsolutePath().normalize();
    }

    public Path currentStorePath() {
        return currentWorkingDirectory()
                .resolve(properties.getSession().getDirectory())
                .resolve(properties.getSession().getStoreFile());
    }

    public SessionStore loadCurrentStoreForContinue() {
        return loadForContinue(currentStorePath());
    }

    public SessionStore appendExchangeToLatestCurrentSession(@NonNull SessionStore.SessionExchange exchange) {
        Path storePath = currentStorePath();
        log.debug(
                "Appending exchange to latest current session at {} with {} tool parts",
                storePath,
                exchange.getToolParts().size());
        SessionStore updatedStore = loadForContinue(storePath)
                .appendExchangeToLatestSession(exchange, sessionStoreClock.now(), sessionStoreClock.now());
        save(storePath, updatedStore);
        return updatedStore;
    }

    public SessionStore saveExchangeToCurrentSession(
            boolean continueSession,
            @NonNull String prompt,
            @NonNull String response) {
        return saveExchangeToCurrentSession(continueSession, prompt, response, List.of());
    }

    public SessionStore saveExchangeToCurrentSession(
            boolean continueSession,
            @NonNull String prompt,
            @NonNull String response,
            List<ToolSessionPart> toolParts) {
        List<ToolSessionPart> normalizedToolParts = toolParts == null ? List.of() : List.copyOf(toolParts);
        Path storePath = currentStorePath();
        SessionStore store = loadExistingOrCreateStore(storePath);
        int sessionIndex;
        if (continueSession && !store.getSessions().isEmpty()) {
            sessionIndex = store.latestSessionIndex();
            log.debug(
                    "Continuing session {} at {} with {} tool parts",
                    store.getSessions().get(sessionIndex).id(),
                    storePath,
                    normalizedToolParts.size());
        }
        else {
            store = store.addNewSession(sessionStoreClock.now());
            sessionIndex = store.getSessions().size() - 1;
            log.debug(
                    "Starting new session {} at {} with {} tool parts",
                    store.getSessions().get(sessionIndex).id(),
                    storePath,
                    normalizedToolParts.size());
        }

        SessionStore updatedStore = store.appendExchangeToSession(
                SessionStore.SessionExchange.builder()
                        .prompt(prompt)
                        .response(response)
                        .toolParts(normalizedToolParts)
                        .build(),
                sessionIndex,
                sessionStoreClock.now(),
                sessionStoreClock.now());
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
        log.debug("Saved session store {} with {} sessions", storePath, store.getSessions().size());
    }

    private SessionStore loadExistingOrCreateStore(Path storePath) {
        if (!Files.exists(storePath)) {
            log.debug("Creating new session store at {}", storePath);
            return newStore();
        }

        try {
            SessionStore store = load(storePath);
            log.debug("Loaded session store {} with {} sessions", storePath, store.getSessions().size());
            return store;
        }
        catch (IOException exception) {
            log.debug("Failed to load session store at {}", storePath, exception);
            throw new NoSessionToContinueException(NO_SESSION_TO_CONTINUE_MESSAGE, exception);
        }
    }

    private SessionStore newStore() {
        Instant now = sessionStoreClock.now();
        return SessionStore.newStore(currentWorkingDirectory().toString(), now);
    }

    private SessionStore loadForContinue(Path storePath) {
        if (!Files.exists(storePath)) {
            log.debug("Creating new session store at {} for continue", storePath);
            SessionStore store = newStore().addNewSession(sessionStoreClock.now());
            save(storePath, store);
            return store;
        }

        try {
            SessionStore store = load(storePath);
            if (store.getSessions().isEmpty()) {
                log.debug("Creating first session in empty store {} for continue", storePath);
                store = store.addNewSession(sessionStoreClock.now());
                save(storePath, store);
            }
            log.debug("Loaded continuation session store {} with {} sessions", storePath, store.getSessions().size());
            return store;
        }
        catch (IOException exception) {
            log.debug("Failed to load continuation session store at {}", storePath, exception);
            throw new NoSessionToContinueException(NO_SESSION_TO_CONTINUE_MESSAGE, exception);
        }
    }

}
