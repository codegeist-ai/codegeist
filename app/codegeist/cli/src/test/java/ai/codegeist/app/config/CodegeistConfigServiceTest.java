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
import org.springframework.context.ApplicationContext;

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
    private ApplicationContext applicationContext;

    @Autowired
    private CodegeistConfig primaryConfig;

    @TempDir
    private Path tempDir;

    @Test
    void exposesPrimaryCodegeistConfigBeanFromInjectedConfigPath() {
        ProviderConfig provider = provider(primaryConfig);

        assertThat(providers(primaryConfig).getConfig().getElements()).hasSize(1);
        assertThat(provider.getType()).isEqualTo(OPENAI_PROVIDER_ID);
        OpenAiProviderConfig openai = (OpenAiProviderConfig) provider;
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
        ProviderConfig provider = provider(config);

        assertThat(provider.getType()).isEqualTo(OPENAI_PROVIDER_ID);
        OpenAiProviderConfig openai = (OpenAiProviderConfig) provider;
        assertThat(openai.getApiKey()).isEqualTo("injected-openai-key");
    }

    @Test
    void rootElementsAreNotSpringBeans() {
        assertThat(applicationContext.getBeansOfType(ProvidersRootElement.class)).isEmpty();
        assertThat(applicationContext.getBeansOfType(McpClientsRootElement.class)).isEmpty();
        assertThat(applicationContext.getBeansOfType(WorkspaceRootElement.class)).isEmpty();
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
        ProviderConfig provider = provider(config);

        assertThat(provider).isInstanceOf(OpenAiProviderConfig.class);
        assertThat(provider.getType()).isEqualTo(OPENAI_PROVIDER_ID);
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
        McpClientsRootElement mcp = mcp(config);
        McpClientConfig client = mcp.getConfig().getElements().getFirst();
        StdioMcpClientConfig stdio = (StdioMcpClientConfig) client;
        Map<?, ?> rendered = renderedYaml(config);
        Map<?, ?> clients = (Map<?, ?>) rendered.get("mcp");
        Map<?, ?> filesystem = (Map<?, ?>) clients.get("filesystem");

        assertThat(mcp.getConfig().getElements()).hasSize(1);
        assertThat(client).isInstanceOf(StdioMcpClientConfig.class);
        assertThat(client.getId()).isEqualTo("filesystem");
        assertThat(client.getType()).isEqualTo(McpClientConfig.Type.stdio);
        assertThat(stdio.getCommand()).isEqualTo("npx");
        assertThat(stdio.getArgs()).containsExactly("-y", "@modelcontextprotocol/server-filesystem", ".");
        assertThat(clients.keySet().stream().map(Object::toString).toList()).containsOnly("filesystem");
        assertThat(filesystem.containsKey("id")).isFalse();
        assertThat(filesystem.get("type")).isEqualTo("stdio");
        assertThat(filesystem.get("command")).isEqualTo("npx");
        assertThat(filesystem.get("args"))
                .isEqualTo(stdio.getArgs());
    }

    @Test
    void keepsMultipleMcpClientsWithSameType() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            mcp:
              filesystem:
                type: stdio
                command: npx
              git:
                type: stdio
                command: uvx
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());
        Map<?, ?> rendered = renderedYaml(config);
        Map<?, ?> clients = (Map<?, ?>) rendered.get("mcp");
        Map<?, ?> filesystem = (Map<?, ?>) clients.get("filesystem");
        Map<?, ?> git = (Map<?, ?>) clients.get("git");

        assertThat(mcp(config).getConfig().getElements().stream().map(McpClientConfig::getId).toList())
                .containsExactly("filesystem", "git");
        assertThat(clients.keySet().stream().map(Object::toString).toList()).containsOnly("filesystem", "git");
        assertThat(filesystem.containsKey("id")).isFalse();
        assertThat(filesystem.get("type")).isEqualTo("stdio");
        assertThat(filesystem.get("command")).isEqualTo("npx");
        assertThat(git.containsKey("id")).isFalse();
        assertThat(git.get("type")).isEqualTo("stdio");
        assertThat(git.get("command")).isEqualTo("uvx");
    }

    @Test
    void loadsStreamableHttpMcpClientFromYamlPath() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            mcp:
              remote-smoke:
                type: streamable_http
                url: http://127.0.0.1:3000
                endpoint: /mcp
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());
        McpClientConfig client = mcp(config).getConfig().getElements().getFirst();
        StreamableHttpMcpClientConfig remoteConfig = (StreamableHttpMcpClientConfig) client;
        Map<?, ?> rendered = renderedYaml(config);
        Map<?, ?> clients = (Map<?, ?>) rendered.get("mcp");
        Map<?, ?> remote = (Map<?, ?>) clients.get("remote-smoke");

        assertThat(client).isInstanceOf(StreamableHttpMcpClientConfig.class);
        assertThat(client.getId()).isEqualTo("remote-smoke");
        assertThat(client.getType()).isEqualTo(McpClientConfig.Type.streamable_http);
        assertThat(remoteConfig.getUrl()).isEqualTo("http://127.0.0.1:3000");
        assertThat(remoteConfig.getEndpoint()).isEqualTo("/mcp");
        assertThat(remote.get("type")).isEqualTo("streamable_http");
        assertThat(remote.get("url")).isEqualTo("http://127.0.0.1:3000");
        assertThat(remote.get("endpoint")).isEqualTo("/mcp");
        assertThat(remote.containsKey("id")).isFalse();
    }

    @Test
    void rejectsStdioMcpClientWithoutCommand() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            mcp:
              filesystem:
                type: stdio
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining(CodegeistConfigService.VALIDATION_ERROR_PREFIX)
                .hasMessageContaining("command")
                .hasMessageContaining("must not be blank");
    }

    @Test
    void rejectsMcpClientWithoutType() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            mcp:
              filesystem:
                command: npx
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining("mcp entry type is required");
    }

    @Test
    void rejectsNullMcpClientEntry() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            mcp:
              filesystem:
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining("mcp entry must be a YAML object");
    }

    @Test
    void rejectsStreamableHttpMcpClientWithoutUrl() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            mcp:
              remote:
                type: streamable_http
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining(CodegeistConfigService.VALIDATION_ERROR_PREFIX)
                .hasMessageContaining("url")
                .hasMessageContaining("must not be blank");
    }

    @Test
    void rejectsUnsupportedMcpClientType() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            mcp:
              legacy:
                type: sse
                url: http://127.0.0.1:3000
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining("Unsupported mcp entry type: sse");
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
        Map<?, ?> clients = (Map<?, ?>) rendered.get("mcp");
        Map<?, ?> grep = (Map<?, ?>) clients.get("grep");
        assertThat(clients.keySet().stream().map(Object::toString).toList()).containsOnly("grep");
        assertThat(grep.get("type")).isEqualTo("stdio");
        assertThat(grep.get("command")).isEqualTo("npx");
        assertThat(grep.containsKey("id")).isFalse();
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
        ProviderConfig provider = provider(config);

        assertThat(provider.getType()).isEqualTo(OLLAMA_PROVIDER_ID);
        assertThat(provider.getName()).isNull();
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

    private ProviderConfig provider(CodegeistConfig config) {
        return config.defaultProvider().orElseThrow();
    }

    private McpClientsRootElement mcp(CodegeistConfig config) {
        return config.rootElement(McpClientsRootElement.class).orElseThrow();
    }

    private Map<?, ?> renderedYaml(CodegeistConfig config) throws IOException {
        return new YAMLMapper().readValue(service.toYaml(config), Map.class);
    }
}
