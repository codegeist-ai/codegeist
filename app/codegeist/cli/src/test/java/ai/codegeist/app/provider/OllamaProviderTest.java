package ai.codegeist.app.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.codegeist.app.chat.CodegeistChatRequest;
import ai.codegeist.app.chat.CodegeistChatResponse;
import ai.codegeist.app.chat.CodegeistChatService;
import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistConfigService;
import ai.codegeist.app.config.CodegeistConfigValidationException;
import ai.codegeist.app.config.OllamaProviderConfig;
import ai.codegeist.app.config.ProviderConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OllamaProviderTest {

    private static final String PROVIDER_ID = "ollama";
    private static final String OLLAMA_BASE_URL = "http://localhost:11434";
    private static final String OLLAMA_MODEL = "llama3.2:1b";
    private static final String PROMPT = "Respond with exactly the lowercase word codegeist. No explanation.";

    @Autowired
    private CodegeistConfigService configService;

    @Autowired
    private CodegeistChatService chatService;

    @TempDir
    private Path tempDir;

    @Test
    void testProviderConfig() throws Exception {
        CodegeistConfig config = loadConfig("""
            provider:
              ollama:
                type: ollama
                base-url: http://localhost:11434
            """);

        ProviderConfig provider = provider(config);
        assertThat(provider).isInstanceOf(OllamaProviderConfig.class);
        assertThat(provider.getBaseUrl()).isEqualTo("http://localhost:11434");
    }

    @Test
    void testMissingBaseUrlFailsConfig() {
        assertThatThrownBy(() -> loadConfig("""
            provider:
              ollama:
                type: ollama
            """))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining("base-url")
                .hasMessageContaining("must not be blank");
    }

    @Test
    @ProviderCategory(ProviderTestCategory.local)
    void testChat() throws Exception {
        CodegeistConfig config = loadConfig("""
            provider:
              ollama:
                type: ollama
                base-url: %s
            """.formatted(OLLAMA_BASE_URL));

        CodegeistChatResponse response = chatService.chat(
                provider(config), new CodegeistChatRequest(OLLAMA_MODEL, PROMPT));

        assertThat(response.content()).containsIgnoringCase("codegeist");
    }

    private CodegeistConfig loadConfig(String yaml) throws Exception {
        Path configFile = tempDir.resolve("codegeist.yml");
        Files.writeString(configFile, yaml);
        return configService.loadConfig(configFile.toString());
    }

    private ProviderConfig provider(CodegeistConfig config) {
        ProviderConfig provider = config.defaultProvider().orElseThrow();
        assertThat(provider.getType()).isEqualTo(PROVIDER_ID);
        return provider;
    }

}
