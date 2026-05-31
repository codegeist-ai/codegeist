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
    private static final String PROVIDER_ID = "ollama";
    private static final String PROVIDER_NAME = "Ollama";

    @Autowired
    private CodegeistConfigService service;

    @Autowired
    private CodegeistConfig mergedConfig;

    @TempDir
    private Path tempDir;

    @Test
    void receivesCodegeistConfigFromApplicationYaml() {
        CodegeistConfig config = service.getSpringBoundConfig();

        assertThat(config).isNotNull();
        assertThat(config.getProvider()).containsOnlyKeys(PROVIDER_ID);
        assertThat(config.getProvider().get(PROVIDER_ID).getName()).isEqualTo(PROVIDER_NAME);
    }

    @Test
    void exposesMergedCodegeistConfigBean() {
        assertThat(mergedConfig).isSameAs(service.getSpringBoundConfig());
    }

    @Test
    void loadsCodegeistConfigFromYamlPath() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              ollama:
                name: Ollama
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());

        assertThat(config.getProvider()).containsOnlyKeys(PROVIDER_ID);
        assertThat(config.getProvider().get(PROVIDER_ID).getName()).isEqualTo(PROVIDER_NAME);
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
              ollama: {}
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());

        assertThat(config.getProvider()).containsOnlyKeys(PROVIDER_ID);
        assertThat(config.getProvider().get(PROVIDER_ID).getName()).isNull();
    }

    @Test
    void rejectsBlankProviderId() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              "":
                name: Ollama
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
                name: "   "
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining(CodegeistConfigService.VALIDATION_ERROR_PREFIX)
                .hasMessageContaining("must not be blank when set");
    }
}
