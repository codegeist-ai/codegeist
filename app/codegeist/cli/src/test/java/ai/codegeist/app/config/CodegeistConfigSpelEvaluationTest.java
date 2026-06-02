package ai.codegeist.app.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CodegeistConfigSpelEvaluationTest {

    private static final String CONFIG_FILE_NAME = "codegeist.yml";
    private static final String LITERAL_PROVIDER_ID = "#{literal-provider-key}";

    @Autowired
    private CodegeistConfigService service;

    @TempDir
    private Path tempDir;

    @Test
    void evaluatesSpelOnlyInStringScalarValues() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              "#{literal-provider-key}":
                type: ollama
                enabled: "#{true}"
                model: "#{'llama' + '3.2:1b'}"
                base-url: "#{'http://localhost:' + (11000 + 434)}"
                options:
                  temperature: "#{0}"
                  retries: 2
                  label: "local-#{'ollama'}"
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());
        ProviderConfig provider = config.getProvider().get(LITERAL_PROVIDER_ID);

        assertThat(config.getProvider()).containsOnlyKeys(LITERAL_PROVIDER_ID);
        assertThat(provider).isInstanceOf(OllamaProviderConfig.class);
        assertThat(provider.getEnabled()).isTrue();
        assertThat(provider.getModel()).isEqualTo("llama3.2:1b");
        assertThat(provider.getBaseUrl()).isEqualTo("http://localhost:11434");
        assertThat(provider.getOptions()).containsEntry("temperature", 0)
                .containsEntry("retries", 2)
                .containsEntry("label", "local-ollama");
    }

    @Test
    void spelNullResultIsValidatedAfterMapping() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              openai:
                type: openai
                model: gpt-4o-mini
                api-key: "#{null}"
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining(CodegeistConfigService.VALIDATION_ERROR_PREFIX)
                .hasMessageContaining("apiKey")
                .hasMessageContaining("must not be blank");
    }

    @Test
    void spelFailuresIncludeSourceAndYamlPathWithoutSecretMaterial() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              openai:
                type: openai
                model: gpt-4o-mini
                api-key: "#{'super-secret-material'.missingMethod(}"
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining(configFile.toString())
                .hasMessageContaining("provider.openai.api-key")
                .hasMessageNotContaining("super-secret-material");
    }
}
