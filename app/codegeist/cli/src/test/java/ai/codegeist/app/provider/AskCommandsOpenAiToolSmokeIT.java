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
 * Explicit OpenAI smoke for the full ask -> OpenAI -> local tool callback path.
 *
 * <p>This class is intentionally not picked up by the default Surefire patterns and
 * is gated by {@link ProviderTestCategory#remote_paid}. It can spend hosted-provider
 * quota, so run it only with an intentional category and API-key decision. The test
 * verifies the durable file and session-store side effects instead of relying on the
 * final model wording.
 */
@ExtendWith(OutputCaptureExtension.class)
@ProviderCategory(ProviderTestCategory.remote_paid)
@SpringBootTest(
    classes = CodegeistApplication.class,
    webEnvironment = WebEnvironment.NONE,
    args = {AskCommandsOpenAiToolSmokeIT.ASK_COMMAND, AskCommandsOpenAiToolSmokeIT.PROMPT}
)
class AskCommandsOpenAiToolSmokeIT {

    static final String ASK_COMMAND = "ask";

    private static final String PROVIDER_ID = "openai";
    private static final String TOOL_NAME = "codegeist_write";
    private static final String TOOL_FILE_NAME = "openai-tool-smoke.txt";
    private static final String TOOL_FILE_CONTENT = "openai-tool-smoke-ok";
    private static final String OPENAI_API_KEY_ENV = "CODEGEIST_TEST_OPENAI_APIKEY";
    private static final String OPENAI_BASE_URL_ENV = "CODEGEIST_TEST_OPENAI_BASE_URL";
    private static final String OPENAI_ORGANIZATION_ID_ENV = "CODEGEIST_TEST_OPENAI_ORGANIZATION_ID";
    private static final String OPENAI_PROJECT_ID_ENV = "CODEGEIST_TEST_OPENAI_PROJECT_ID";
    private static final String SESSION_DIRECTORY = "target/provider-tests/ask-command-openai-tool-session";
    private static final String WORKSPACE_DIRECTORY = "target/provider-tests/ask-command-openai-tool-workspace";
    private static final Path CONFIG_FILE = Path.of("target", "provider-tests", "ask-command-openai-tool.yml");
    private static final Path SESSION_DIRECTORY_PATH = Path.of(SESSION_DIRECTORY);
    private static final Path WORKSPACE_DIRECTORY_PATH = Path.of(WORKSPACE_DIRECTORY);
    private static final Path TOOL_FILE = WORKSPACE_DIRECTORY_PATH.resolve(TOOL_FILE_NAME);

    static final String PROMPT = "You are testing Codegeist tool calling. Call exactly the tool named "
            + TOOL_NAME + " with arguments {\"path\":\"" + TOOL_FILE_NAME
            + "\",\"content\":\"" + TOOL_FILE_CONTENT
            + "\"}. Do not write or simulate a tool call as text. Do not call any other tool. "
            + "After the tool returns, answer with exactly: openai tool smoke completed";

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
    void askCommandCanCallLocalToolThroughOpenAi(CapturedOutput output) throws IOException {
        assertThat(output.getErr()).isEmpty();
        assertThat(output.getOut()).isNotBlank();
        assertThat(TOOL_FILE).hasContent(TOOL_FILE_CONTENT);

        SessionStore store = sessionStoreService.load(sessionStoreService.currentStorePath());
        List<ToolSessionPart> toolParts = store.getSessions().stream()
                .flatMap(session -> session.messages().stream())
                .flatMap(message -> message.parts().stream())
                .filter(ToolSessionPart.class::isInstance)
                .map(ToolSessionPart.class::cast)
                .toList();

        assertThat(toolParts).anySatisfy(part -> {
            assertThat(part.getTool()).isEqualTo(TOOL_NAME);
            assertThat(part.getStatus()).isEqualTo(ToolSessionPartStatus.completed);
            assertThat(part.getOutputPreview()).contains(TOOL_FILE_NAME);
        });
    }

    @SneakyThrows(IOException.class)
    private static void writeConfigFile() {
        FileSystemUtils.deleteRecursively(SESSION_DIRECTORY_PATH);
        FileSystemUtils.deleteRecursively(WORKSPACE_DIRECTORY_PATH);
        Files.createDirectories(CONFIG_FILE.getParent());
        Files.createDirectories(WORKSPACE_DIRECTORY_PATH);
        Files.writeString(CONFIG_FILE, """
            provider:
              %s:
                type: openai
                api-key: "#{T(java.lang.System).getenv('%s')}"
                base-url: "#{T(java.lang.System).getenv('%s')}"
                organization-id: "#{T(java.lang.System).getenv('%s')}"
                project-id: "#{T(java.lang.System).getenv('%s')}"
            workspace:
              directory: %s
            """.formatted(
                PROVIDER_ID,
                OPENAI_API_KEY_ENV,
                OPENAI_BASE_URL_ENV,
                OPENAI_ORGANIZATION_ID_ENV,
                OPENAI_PROJECT_ID_ENV,
                WORKSPACE_DIRECTORY));
    }
}
