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
    private static final String OLLAMA_PROVIDER_TYPE = OllamaProviderConfig.PROVIDER_TYPE;

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
                name: "local-#{'ollama'}"
                base-url: "#{'http://localhost:' + (11000 + 434)}"
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());
        ProvidersRootElement providers = providers(config);
        ProviderConfig provider = providers.getProviders().get(OLLAMA_PROVIDER_TYPE);

        assertThat(providers.getProviders()).containsOnlyKeys(OLLAMA_PROVIDER_TYPE);
        assertThat(provider).isInstanceOf(OllamaProviderConfig.class);
        assertThat(provider.getName()).isEqualTo("local-ollama");
        assertThat(provider.getBaseUrl()).isEqualTo("http://localhost:11434");
    }

    @Test
    void spelNullResultIsValidatedAfterMapping() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              openai:
                type: openai
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
                api-key: "#{'super-secret-material'.missingMethod(}"
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining(configFile.toString())
                .hasMessageContaining("provider.openai.api-key")
                .hasMessageNotContaining("super-secret-material");
    }

    private ProvidersRootElement providers(CodegeistConfig config) {
        return config.rootElement(ProvidersRootElement.class).orElseThrow();
    }
}
