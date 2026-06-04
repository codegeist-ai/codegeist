package ai.codegeist.app.provider;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistApplication;
import ai.codegeist.app.config.CodegeistConfigService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ExtendWith(OutputCaptureExtension.class)
@ProviderCategory(ProviderTestCategory.local)
@SpringBootTest(
    classes = CodegeistApplication.class,
    webEnvironment = WebEnvironment.NONE,
    args = {AskCommandsTest.ASK_COMMAND, AskCommandsTest.PROMPT}
)
class AskCommandsTest {

    static final String ASK_COMMAND = "ask";
    private static final String OLLAMA_BASE_URL = "http://localhost:11434";
    static final String PROMPT = "codegeist";
    private static final Path CONFIG_FILE = Path.of("target", "provider-tests", "ask-command-codegeist.yml");

    @DynamicPropertySource
    static void codegeistConfig(DynamicPropertyRegistry registry) {
        writeConfigFile();
        registry.add(CodegeistConfigService.CONFIG_PROPERTY, CONFIG_FILE::toString);
    }

    @Test
    void askCommandUsesGlobalConfigAndLocalOllama(CapturedOutput output) {
        assertThat(output.getOut()).containsIgnoringCase("codegeist");
        assertThat(output.getErr()).isEmpty();
    }

    @SneakyThrows(IOException.class)
    private static void writeConfigFile() {
        Files.createDirectories(CONFIG_FILE.getParent());
        Files.writeString(CONFIG_FILE, """
            provider:
              ollama:
                type: ollama
                base-url: %s
            """.formatted(OLLAMA_BASE_URL));
    }
}
