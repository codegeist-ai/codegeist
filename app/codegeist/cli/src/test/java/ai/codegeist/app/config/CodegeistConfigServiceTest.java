package ai.codegeist.app.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = CodegeistConfigService.CONFIG_PROPERTY + "=src/test/resources/codegeist-current-config-test.yml"
)
class CodegeistConfigServiceTest {

    private static final String CONFIG_FILE_NAME = "codegeist.yml";
    private static final String OLLAMA_PROVIDER_ID = "ollama";
    private static final String OPENAI_PROVIDER_ID = "openai";
    @Autowired
    private CodegeistConfigService service;

    @Autowired
    private CodegeistConfig primaryConfig;

    @TempDir
    private Path tempDir;

    @Test
    void exposesPrimaryCodegeistConfigBeanFromInjectedConfigPath() {
        Map<String, ProviderConfig> providers = providers(primaryConfig).getProviders();

        assertThat(providers).containsOnlyKeys(OPENAI_PROVIDER_ID);
        OpenAiProviderConfig openai = (OpenAiProviderConfig) providers.get(OPENAI_PROVIDER_ID);
        assertThat(openai.getApiKey()).isEqualTo("injected-openai-key");
        assertThat(primaryConfig.rootElement(McpClientsRootElement.class)).isEmpty();
    }

    @Test
    void primaryConfigRendersAsDirectYaml() {
        assertThat(service.toYaml(primaryConfig)).contains("provider:").contains("injected-openai-key");
    }

    @Test
    void usesInjectedConfigPathAsCurrentConfig() {
        CodegeistConfig config = service.loadCurrentConfig();
        Map<String, ProviderConfig> providers = providers(config).getProviders();

        assertThat(providers).containsOnlyKeys(OPENAI_PROVIDER_ID);
        OpenAiProviderConfig openai = (OpenAiProviderConfig) providers.get(OPENAI_PROVIDER_ID);
        assertThat(openai.getApiKey()).isEqualTo("injected-openai-key");
    }

    @Test
    void loadsCodegeistConfigFromYamlPath() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              openai:
                type: openai
                name: OpenAI
                api-key: local-openai-key
                organization-id: org-local
                project-id: project-local
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());
        Map<String, ProviderConfig> providers = providers(config).getProviders();

        assertThat(providers).containsOnlyKeys(OPENAI_PROVIDER_ID);
        ProviderConfig provider = providers.get(OPENAI_PROVIDER_ID);
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

        assertThat(yaml.trim()).isEqualTo("{}");
        assertThat(yaml).doesNotContain("---");
        assertThat(yaml).doesNotContain("codegeist:");
    }

    @Test
    void loadsMcpClientsRootElementFromYamlPath() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            mcp:
              filesystem:
                type: stdio
                command: npx
                args:
                  - -y
                  - "@modelcontextprotocol/server-filesystem"
                  - .
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());
        Map<String, McpClientConfig> clients = mcp(config).getClients();

        assertThat(clients).containsOnlyKeys("filesystem");
        assertThat(clients.get("filesystem").getType()).isEqualTo("stdio");
        assertThat(clients.get("filesystem").getCommand()).isEqualTo("npx");
        assertThat(clients.get("filesystem").getArgs())
                .containsExactly("-y", "@modelcontextprotocol/server-filesystem", ".");
    }

    @Test
    void writesMcpRootAsDirectYaml() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              ollama:
                type: ollama
                base-url: http://localhost:11434
            mcp:
              grep:
                type: stdio
                command: npx
            """);

        String yaml = service.toYaml(service.loadConfig(configFile.toString()));
        Map<?, ?> rendered = new YAMLMapper().readValue(yaml, Map.class);

        assertThat(rendered.keySet().stream().map(Object::toString).toList())
                .containsExactlyInAnyOrder("provider", "mcp");
        assertThat(((Map<?, ?>) rendered.get("mcp")).keySet().stream().map(Object::toString).toList())
                .containsExactly("grep");
    }

    @Test
    void allowsProviderWithoutName() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              ollama:
                type: ollama
                base-url: http://localhost:11434
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());
        Map<String, ProviderConfig> providers = providers(config).getProviders();

        assertThat(providers).containsOnlyKeys(OLLAMA_PROVIDER_ID);
        assertThat(providers.get(OLLAMA_PROVIDER_ID).getName()).isNull();
    }

    @Test
    void rejectsBlankProviderId() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              "":
                type: ollama
                name: Ollama
                base-url: http://localhost:11434
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining("provider entry id must not be blank");
    }

    @Test
    void rejectsBlankProviderNameWhenSet() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            provider:
              ollama:
                type: ollama
                name: "   "
                base-url: http://localhost:11434
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining(CodegeistConfigService.VALIDATION_ERROR_PREFIX)
                .hasMessageContaining("must not be blank when set");
    }

    private ProvidersRootElement providers(CodegeistConfig config) {
        return config.rootElement(ProvidersRootElement.class).orElseThrow();
    }

    private McpClientsRootElement mcp(CodegeistConfig config) {
        return config.rootElement(McpClientsRootElement.class).orElseThrow();
    }
}
