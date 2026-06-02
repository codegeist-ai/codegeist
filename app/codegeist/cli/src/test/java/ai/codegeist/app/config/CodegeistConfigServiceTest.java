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
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("codegeist-config-service-test")
class CodegeistConfigServiceTest {

    private static final String CONFIG_FILE_NAME = "codegeist.yml";
    private static final String OLLAMA_PROVIDER_ID = "ollama";
    private static final String OPENAI_PROVIDER_ID = "openai";
    private static final String PROVIDER_NAME = "Ollama";
    private static final String OLLAMA_MODEL = "llama3.2:1b";

    @Autowired
    private CodegeistConfigService service;

    @Autowired
    private CodegeistConfig primaryConfig;

    @TempDir
    private Path tempDir;

    @Test
    void receivesCodegeistConfigFromApplicationYaml() {
        CodegeistConfig config = service.getSpringBoundConfig();

        assertThat(config).isNotNull();
        assertThat(config.getProvider()).containsOnlyKeys(OLLAMA_PROVIDER_ID, OPENAI_PROVIDER_ID);

        ProviderConfig ollama = config.getProvider().get(OLLAMA_PROVIDER_ID);
        assertThat(ollama).isInstanceOf(OllamaProviderConfig.class);
        assertThat(ollama.getType()).isEqualTo(OLLAMA_PROVIDER_ID);
        assertThat(ollama.getName()).isEqualTo(PROVIDER_NAME);
        assertThat(ollama.getModel()).isEqualTo(OLLAMA_MODEL);
        assertThat(ollama.getBaseUrl()).isEqualTo("http://localhost:11434");
        assertThat(ollama.getOptions()).containsEntry("temperature", 0).containsEntry("seed", 7);

        ProviderConfig openai = config.getProvider().get(OPENAI_PROVIDER_ID);
        assertThat(openai).isInstanceOf(OpenAiProviderConfig.class);
        assertThat(((OpenAiProviderConfig) openai).getApiKey()).isEqualTo("test-openai-secret");
    }

    @Test
    void exposesPrimaryCodegeistConfigBean() {
        assertThat(primaryConfig).isSameAs(service.getSpringBoundConfig());
    }

    @Test
    void loadsCodegeistConfigFromYamlPath() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              openai:
                type: openai
                name: OpenAI
                enabled: true
                model: gpt-4o-mini
                api-key: local-openai-key
                organization-id: org-local
                project-id: project-local
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());

        assertThat(config.getProvider()).containsOnlyKeys(OPENAI_PROVIDER_ID);
        ProviderConfig provider = config.getProvider().get(OPENAI_PROVIDER_ID);
        assertThat(provider).isInstanceOf(OpenAiProviderConfig.class);
        assertThat(provider.getName()).isEqualTo("OpenAI");
        OpenAiProviderConfig openai = (OpenAiProviderConfig) provider;
        assertThat(openai.getApiKey()).isEqualTo("local-openai-key");
        assertThat(openai.getOrganizationId()).isEqualTo("org-local");
        assertThat(openai.getProjectId()).isEqualTo("project-local");
    }

    @Test
    void writesCodegeistConfigAsDirectYaml() {
        String yaml = service.toYaml(new CodegeistConfig());

        assertThat(yaml).contains("provider: {}");
        assertThat(yaml).doesNotContain("---");
        assertThat(yaml).doesNotContain("codegeist:");
    }

    @Test
    void allowsProviderWithoutName() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              ollama:
                type: ollama
                model: llama3.2:1b
                base-url: http://localhost:11434
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());

        assertThat(config.getProvider()).containsOnlyKeys(OLLAMA_PROVIDER_ID);
        assertThat(config.getProvider().get(OLLAMA_PROVIDER_ID).getName()).isNull();
    }

    @Test
    void rejectsBlankProviderId() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              "":
                type: ollama
                name: Ollama
                model: llama3.2:1b
                base-url: http://localhost:11434
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining(CodegeistConfigService.VALIDATION_ERROR_PREFIX)
                .hasMessageContaining("must not be blank");
    }

    @Test
    void rejectsBlankProviderNameWhenSet() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              ollama:
                type: ollama
                name: "   "
                model: llama3.2:1b
                base-url: http://localhost:11434
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining(CodegeistConfigService.VALIDATION_ERROR_PREFIX)
                .hasMessageContaining("must not be blank when set");
    }
}
