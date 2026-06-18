package ai.codegeist.app.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.codegeist.app.CodegeistSpringAppProperties;
import ai.codegeist.app.session.ToolSessionPart.ToolSessionPartStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SessionStoreServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-06-09T12:00:00Z");
    private static final Instant OLDER_UPDATED_AT = Instant.parse("2026-06-09T12:01:00Z");
    private static final Instant LATEST_UPDATED_AT = Instant.parse("2026-06-09T12:02:00Z");
    private static final Instant APPEND_AT = Instant.parse("2026-06-09T12:03:00Z");
    private static final UUID OLDER_SESSION_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID LATEST_SESSION_ID = UUID.fromString("00000000-0000-4000-8000-000000000002");
    private static final UUID COMPACTION_SESSION_ID = UUID.fromString("00000000-0000-4000-8000-000000000003");
    private static final UUID COMPACTION_MESSAGE_ID = UUID.fromString("00000000-0000-4000-8000-000000000004");
    private static final UUID SUMMARY_MESSAGE_ID = UUID.fromString("00000000-0000-4000-8000-000000000005");
    private static final UUID COMPACTION_PART_ID = UUID.fromString("00000000-0000-4000-8000-000000000006");
    private static final UUID SUMMARY_PART_ID = UUID.fromString("00000000-0000-4000-8000-000000000007");
    private static final UUID TAIL_START_MESSAGE_ID = UUID.fromString("00000000-0000-4000-8000-000000000008");
    private static final UUID TOOL_SESSION_ID = UUID.fromString("00000000-0000-4000-8000-000000000009");
    private static final UUID TOOL_MESSAGE_ID = UUID.fromString("00000000-0000-4000-8000-000000000010");
    private static final UUID TOOL_TEXT_PART_ID = UUID.fromString("00000000-0000-4000-8000-000000000011");
    private static final UUID TOOL_PART_ID = UUID.fromString("00000000-0000-4000-8000-000000000012");
    private static final String READ_TOOL_NAME = "codegeist_read";
    private static final String TOOL_OUTPUT_PREVIEW = "1: hello";

    @TempDir
    private Path tempDir;

    private CodegeistSpringAppProperties properties;
    private SessionStoreService service;

    @BeforeEach
    void setUp() {
        properties = new CodegeistSpringAppProperties();
        service = new SessionStoreService(new SessionStoreObjectMapper(),
                new SessionStoreClock(Clock.fixed(APPEND_AT, ZoneOffset.UTC)),
                properties);
        service.workingDir = tempDir.toString();
    }

    @Test
    void currentWorkingDirectoryUsesUserDir() {
        assertThat(service.currentWorkingDirectory()).isEqualTo(tempDir.toAbsolutePath().normalize());
    }

    @Test
    void currentStorePathUsesDefaultDirectoryAndFile() {
        assertThat(service.currentStorePath())
                .isEqualTo(tempDir.toAbsolutePath().normalize()
                        .resolve(CodegeistSpringAppProperties.DEFAULT_SESSION_DIRECTORY)
                        .resolve(CodegeistSpringAppProperties.DEFAULT_SESSION_STORE_FILE));
    }

    @Test
    void currentStorePathUsesConfiguredDirectoryAndFile() {
        properties.getSession().setDirectory("custom-codegeist");
        properties.getSession().setStoreFile("custom-session.json");

        assertThat(service.currentStorePath())
                .isEqualTo(tempDir.toAbsolutePath().normalize()
                        .resolve("custom-codegeist")
                        .resolve("custom-session.json"));
    }

    @Test
    void currentStorePathUsesDefaultsForBlankConfiguration() {
        properties.getSession().setDirectory(" ");
        properties.getSession().setStoreFile("");

        assertThat(service.currentStorePath())
                .isEqualTo(tempDir.toAbsolutePath().normalize()
                        .resolve(CodegeistSpringAppProperties.DEFAULT_SESSION_DIRECTORY)
                        .resolve(CodegeistSpringAppProperties.DEFAULT_SESSION_STORE_FILE));
    }

    @Test
    void appendsTextExchangeToNewestSessionAndPreservesOlderSessions() throws IOException {
        service.save(service.currentStorePath(), storeWithTwoSessions());

        SessionStore updated = service.saveExchangeToCurrentSession(true, "Fix this test", "I found it.");

        assertThat(updated.getUpdatedAt()).isEqualTo(APPEND_AT);
        assertThat(updated.getSessions()).hasSize(2);
        assertThat(updated.getSessions().get(0).id()).isEqualTo(OLDER_SESSION_ID);
        assertThat(updated.getSessions().get(0).messages()).isEmpty();

        CodegeistSession latest = updated.getSessions().get(1);
        assertThat(latest.id()).isEqualTo(LATEST_SESSION_ID);
        assertThat(latest.updatedAt()).isEqualTo(APPEND_AT);
        assertThat(latest.messages()).hasSize(2);

        SessionMessage userMessage = latest.messages().get(0);
        SessionMessage assistantMessage = latest.messages().get(1);
        assertThat(userMessage.id()).isNotNull();
        assertThat(assistantMessage.id()).isNotNull();
        assertThat(userMessage.role()).isEqualTo(SessionMessageRole.USER);
        assertThat(userMessage.parts()).singleElement().isInstanceOf(TextSessionPart.class);
        assertThat(userMessage.parts().get(0).getId()).isNotNull();
        assertThat(((TextSessionPart) userMessage.parts().get(0)).getText()).isEqualTo("Fix this test");
        assertThat(assistantMessage.role()).isEqualTo(SessionMessageRole.ASSISTANT);
        assertThat(assistantMessage.parentMessageId()).isEqualTo(userMessage.id());
        assertThat(assistantMessage.completedAt()).isEqualTo(APPEND_AT);
        assertThat(assistantMessage.parts()).singleElement().isInstanceOf(TextSessionPart.class);
        assertThat(assistantMessage.parts().get(0).getId()).isNotNull();
        assertThat(((TextSessionPart) assistantMessage.parts().get(0)).getText()).isEqualTo("I found it.");

        SessionStore reloaded = service.load(service.currentStorePath());
        assertThat(reloaded.getSessions().get(1).messages()).hasSize(2);
    }

    @Test
    void missingOrEmptySessionStoreCreatesSessionAndCorruptStoreFails() throws IOException {
        SessionStore missingStore = service.saveExchangeToCurrentSession(true, "Missing", "Created");

        assertThat(missingStore.getSessions()).hasSize(1);
        assertThat(missingStore.getSessions().get(0).id()).isNotNull();
        assertThat(missingStore.getSessions().get(0).messages()).hasSize(2);
        assertThat(Files.exists(service.currentStorePath())).isTrue();

        service.save(service.currentStorePath(), SessionStore.builder()
                .schemaVersion(SessionStore.SCHEMA_VERSION)
                .workingDir(tempDir.toString())
                .createdAt(CREATED_AT)
                .updatedAt(CREATED_AT)
                .sessions(List.of())
                .build());

        SessionStore emptyStore = service.saveExchangeToCurrentSession(true, "Empty", "Created");

        assertThat(emptyStore.getSessions()).hasSize(1);
        assertThat(emptyStore.getSessions().get(0).messages()).hasSize(2);

        Files.writeString(service.currentStorePath(), "not json");

        assertThatThrownBy(() -> service.saveExchangeToCurrentSession(true, "Corrupt", "Ignored"))
                .isInstanceOf(NoSessionToContinueException.class)
                .hasMessage(SessionStoreService.NO_SESSION_TO_CONTINUE_MESSAGE);
    }

    @Test
    void loadCurrentStoreForContinueCreatesSessionWhenMissingOrEmpty() throws IOException {
        SessionStore missingStore = service.loadCurrentStoreForContinue();

        assertThat(missingStore.getSessions()).hasSize(1);
        assertThat(missingStore.getSessions().get(0).messages()).isEmpty();
        assertThat(Files.exists(service.currentStorePath())).isTrue();

        service.save(service.currentStorePath(), SessionStore.builder()
                .schemaVersion(SessionStore.SCHEMA_VERSION)
                .workingDir(tempDir.toString())
                .createdAt(CREATED_AT)
                .updatedAt(CREATED_AT)
                .sessions(List.of())
                .build());

        SessionStore emptyStore = service.loadCurrentStoreForContinue();

        assertThat(emptyStore.getSessions()).hasSize(1);
        assertThat(emptyStore.getSessions().get(0).messages()).isEmpty();
    }

    @Test
    void compactionPartAndSummaryAssistantMessageRoundTrip() throws IOException {
        CompactionSessionPart compactionPart = new CompactionSessionPart();
        compactionPart.setId(COMPACTION_PART_ID);
        compactionPart.setAuto(true);
        compactionPart.setOverflow(true);
        compactionPart.setTailStartMessageId(TAIL_START_MESSAGE_ID);
        SessionMessage compactionMessage = new SessionMessage(
                COMPACTION_MESSAGE_ID,
                SessionMessageRole.USER,
                CREATED_AT,
                null,
                null,
                List.of(compactionPart));
        SessionMessage summaryMessage = new SessionMessage(
                SUMMARY_MESSAGE_ID,
                SessionMessageRole.ASSISTANT,
                CREATED_AT,
                LATEST_UPDATED_AT,
                compactionMessage.id(),
                List.of(new TextSessionPart(SUMMARY_PART_ID, "Summary of earlier work.")));
        SessionStore store = SessionStore.builder()
                .schemaVersion(SessionStore.SCHEMA_VERSION)
                .workingDir(tempDir.toString())
                .createdAt(CREATED_AT)
                .updatedAt(LATEST_UPDATED_AT)
                .sessions(List.of(new CodegeistSession(
                        COMPACTION_SESSION_ID,
                        "Compaction session",
                        CREATED_AT,
                        LATEST_UPDATED_AT,
                        List.of(compactionMessage, summaryMessage))))
                .build();

        service.save(service.currentStorePath(), store);
        SessionStore reloaded = service.load(service.currentStorePath());

        SessionPart part = reloaded.getSessions().get(0).messages().get(0).parts().get(0);
        assertThat(part).isInstanceOf(CompactionSessionPart.class);
        CompactionSessionPart reloadedCompactionPart = (CompactionSessionPart) part;
        assertThat(reloadedCompactionPart.isAuto()).isTrue();
        assertThat(reloadedCompactionPart.isOverflow()).isTrue();
        assertThat(reloadedCompactionPart.getTailStartMessageId()).isEqualTo(TAIL_START_MESSAGE_ID);
        assertThat(reloaded.getSessions().get(0).messages().get(1).parentMessageId()).isEqualTo(compactionMessage.id());
    }

    @Test
    void toolPartRoundTripsThroughSessionStoreJson() throws IOException {
        ToolSessionPart toolPart = completedReadToolPart();
        SessionMessage assistantMessage = new SessionMessage(
                TOOL_MESSAGE_ID,
                SessionMessageRole.ASSISTANT,
                CREATED_AT,
                LATEST_UPDATED_AT,
                null,
                List.of(toolPart, new TextSessionPart(TOOL_TEXT_PART_ID, "Read the file.")));
        SessionStore store = SessionStore.builder()
                .schemaVersion(SessionStore.SCHEMA_VERSION)
                .workingDir(tempDir.toString())
                .createdAt(CREATED_AT)
                .updatedAt(LATEST_UPDATED_AT)
                .sessions(List.of(new CodegeistSession(
                        TOOL_SESSION_ID,
                        "Tool session",
                        CREATED_AT,
                        LATEST_UPDATED_AT,
                        List.of(assistantMessage))))
                .build();

        service.save(service.currentStorePath(), store);
        SessionStore reloaded = service.load(service.currentStorePath());

        SessionPart part = reloaded.getSessions().get(0).messages().get(0).parts().get(0);
        assertThat(part).isInstanceOf(ToolSessionPart.class);
        ToolSessionPart reloadedToolPart = (ToolSessionPart) part;
        assertThat(reloadedToolPart.getId()).isEqualTo(TOOL_PART_ID);
        assertThat(reloadedToolPart.getTool()).isEqualTo(READ_TOOL_NAME);
        assertThat(reloadedToolPart.getStatus()).isEqualTo(ToolSessionPartStatus.completed);
        assertThat(reloadedToolPart.getOutputPreview()).isEqualTo(TOOL_OUTPUT_PREVIEW);
    }

    @Test
    void appendsToolPartsBeforeAssistantTextAndAssignsMissingIds() throws IOException {
        service.save(service.currentStorePath(), storeWithTwoSessions());
        ToolSessionPart toolPart = completedReadToolPart();
        toolPart.setId(null);

        SessionStore updated = service.saveExchangeToCurrentSession(
                true,
                "Read README",
                "The README was read.",
                List.of(toolPart));

        CodegeistSession latest = updated.getSessions().get(1);
        SessionMessage assistantMessage = latest.messages().get(1);
        assertThat(assistantMessage.parts()).hasSize(2);
        assertThat(assistantMessage.parts().get(0)).isInstanceOf(ToolSessionPart.class);
        ToolSessionPart savedToolPart = (ToolSessionPart) assistantMessage.parts().get(0);
        assertThat(savedToolPart.getId()).isNotNull();
        assertThat(savedToolPart.getOutputPreview()).isEqualTo(TOOL_OUTPUT_PREVIEW);
        assertThat(assistantMessage.parts().get(1)).isInstanceOf(TextSessionPart.class);
        assertThat(((TextSessionPart) assistantMessage.parts().get(1)).getText()).isEqualTo("The README was read.");

        SessionStore reloaded = service.load(service.currentStorePath());
        SessionMessage reloadedAssistantMessage = reloaded.getSessions().get(1).messages().get(1);
        assertThat(reloadedAssistantMessage.parts().get(0)).isInstanceOf(ToolSessionPart.class);
        assertThat(reloadedAssistantMessage.parts().get(1)).isInstanceOf(TextSessionPart.class);
    }

    @Test
    void sessionStoreJsonExcludesRuntimeConfigurationAndSecrets() throws IOException {
        service.saveExchangeToCurrentSession(
                false,
                "Hello",
                "World",
                List.of(completedReadToolPart()));

        String json = Files.readString(service.currentStorePath());

        assertThat(json)
                .contains(ToolSessionPart.TYPE)
                .contains(TOOL_OUTPUT_PREVIEW)
                .doesNotContain("api-key")
                .doesNotContain("injected-openai-key")
                .doesNotContain("provider")
                .doesNotContain("selectedProvider")
                .doesNotContain("selectedModel")
                .doesNotContain("mcp")
                .doesNotContain("enabledTools")
                .doesNotContain("permission")
                .doesNotContain("runtimeStatus")
                .doesNotContain("tui");
    }

    private SessionStore storeWithTwoSessions() {
        return SessionStore.builder()
                .schemaVersion(SessionStore.SCHEMA_VERSION)
                .workingDir(tempDir.toString())
                .createdAt(CREATED_AT)
                .updatedAt(LATEST_UPDATED_AT)
                .sessions(List.of(
                        new CodegeistSession(OLDER_SESSION_ID, "Older session", CREATED_AT, OLDER_UPDATED_AT, List.of()),
                        new CodegeistSession(LATEST_SESSION_ID, "Latest session", CREATED_AT, LATEST_UPDATED_AT, List.of())))
                .build();
    }

    private ToolSessionPart completedReadToolPart() {
        ToolSessionPart toolPart = new ToolSessionPart();
        toolPart.setId(TOOL_PART_ID);
        toolPart.setTool(READ_TOOL_NAME);
        toolPart.setStatus(ToolSessionPartStatus.completed);
        toolPart.setOutputPreview(TOOL_OUTPUT_PREVIEW);
        return toolPart;
    }
}
