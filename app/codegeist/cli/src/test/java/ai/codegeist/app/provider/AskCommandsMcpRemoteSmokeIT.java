package ai.codegeist.app.provider;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistApplication;
import ai.codegeist.app.CodegeistSpringAppProperties;
import ai.codegeist.app.config.CodegeistConfigService;
import ai.codegeist.app.session.SessionStore;
import ai.codegeist.app.session.SessionStoreService;
import ai.codegeist.app.session.ToolSessionPart;
import ai.codegeist.app.session.ToolSessionPart.ToolSessionPartStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.FileSystemUtils;

/**
 * Explicit smoke test for the full ask -> Ollama -> MCP callback path.
 *
 * <p>This class is intentionally not picked up by the default Surefire patterns.
 * `task mcp-remote-smoke` starts the Docker MCP fixture, starts local Ollama, sets
 * {@value #REMOTE_SMOKE_URL_PROPERTY}, and then selects this class explicitly. The
 * assertion checks the durable session-store side effect rather than relying only on
 * the final model wording.
 */
@ExtendWith(OutputCaptureExtension.class)
@ProviderCategory(ProviderTestCategory.local)
@EnabledIfSystemProperty(named = AskCommandsMcpRemoteSmokeIT.REMOTE_SMOKE_URL_PROPERTY, matches = ".+")
@SpringBootTest(
    classes = CodegeistApplication.class,
    webEnvironment = WebEnvironment.NONE,
    args = {AskCommandsMcpRemoteSmokeIT.ASK_COMMAND, AskCommandsMcpRemoteSmokeIT.PROMPT}
)
class AskCommandsMcpRemoteSmokeIT {

    static final String ASK_COMMAND = "ask";
    static final String REMOTE_SMOKE_URL_PROPERTY = "codegeist.mcp.remote-smoke.url";

    private static final String OLLAMA_BASE_URL = "http://localhost:11434";
    private static final String OLLAMA_PROVIDER_ID = "ollama";
    private static final String REMOTE_MCP_CLIENT_ID = "remote-smoke";
    private static final String REMOTE_TOOL_NAME = "remote_echo";
    private static final String REMOTE_TOOL_INPUT = "codegeist-mcp-smoke";
    private static final String REMOTE_TOOL_OUTPUT = "remote: " + REMOTE_TOOL_INPUT;
    private static final String SESSION_DIRECTORY = "target/provider-tests/ask-command-mcp-session";
    private static final Path CONFIG_FILE = Path.of("target", "provider-tests", "ask-command-mcp-codegeist.yml");
    private static final Path SESSION_DIRECTORY_PATH = Path.of(SESSION_DIRECTORY);

    static final String PROMPT = "You must call the tool named " + REMOTE_TOOL_NAME
            + " with arguments {\"text\":\"" + REMOTE_TOOL_INPUT
            + "\"}. Do not write or simulate a tool call as text. After the tool returns, answer with exactly "
            + "the tool result and no extra text.";

    @Autowired
    private SessionStoreService sessionStoreService;

    @DynamicPropertySource
    static void codegeistConfig(DynamicPropertyRegistry registry) {
        writeConfigFile();
        registry.add(CodegeistConfigService.CONFIG_PROPERTY, CONFIG_FILE::toString);
        registry.add(CodegeistSpringAppProperties.SESSION_DIRECTORY_PROPERTY, () -> SESSION_DIRECTORY);
        registry.add(
                CodegeistSpringAppProperties.SESSION_STORE_FILE_PROPERTY,
                () -> CodegeistSpringAppProperties.DEFAULT_SESSION_STORE_FILE);
    }

    @Test
    void askCommandCanCallRemoteMcpToolThroughLocalOllama(CapturedOutput output) throws IOException {
        assertThat(output.getErr()).isEmpty();
        assertThat(output.getOut()).isNotBlank();

        SessionStore store = sessionStoreService.load(sessionStoreService.currentStorePath());
        List<ToolSessionPart> toolParts = store.getSessions().stream()
                .flatMap(session -> session.messages().stream())
                .flatMap(message -> message.parts().stream())
                .filter(ToolSessionPart.class::isInstance)
                .map(ToolSessionPart.class::cast)
                .toList();

        assertThat(toolParts).anySatisfy(part -> {
            assertThat(part.getTool()).contains(REMOTE_TOOL_NAME);
            assertThat(part.getStatus()).isEqualTo(ToolSessionPartStatus.completed);
            assertThat(part.getOutputPreview()).contains(REMOTE_TOOL_OUTPUT);
        });
    }

    @SneakyThrows(IOException.class)
    private static void writeConfigFile() {
        FileSystemUtils.deleteRecursively(SESSION_DIRECTORY_PATH);
        Files.createDirectories(CONFIG_FILE.getParent());
        Files.writeString(CONFIG_FILE, """
            provider:
              %s:
                type: ollama
                base-url: %s
            mcp:
              %s:
                type: streamable_http
                url: %s
            """.formatted(
                OLLAMA_PROVIDER_ID,
                OLLAMA_BASE_URL,
                REMOTE_MCP_CLIENT_ID,
                System.getProperty(REMOTE_SMOKE_URL_PROPERTY, "http://127.0.0.1:1")));
    }
}
