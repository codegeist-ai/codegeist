package ai.codegeist.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Keeps tracked contributor examples on the production YAML loading and Bean
 * Validation path. The test loads configuration only; it never creates provider
 * clients, opens MCP transports, or invokes tools.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CodegeistExamplesTest {

    private static final String OPENAI_API_KEY_PLACEHOLDER = "not-a-real-openai-api-key";
    private static final Path EXAMPLES_DIRECTORY = Path.of("..", "..", "..", "examples")
            .toAbsolutePath()
            .normalize();

    @Autowired
    private CodegeistConfigService configService;

    @Test
    void ollamaExampleParsesExpectedLocalProvider() {
        CodegeistConfig config = loadExample("codegeist.ollama.yml");

        assertThat(config.rootElements).singleElement().isInstanceOf(ProvidersRootElement.class);
        assertThat(config.defaultProvider()).hasValueSatisfying(provider -> {
            assertThat(provider).isInstanceOf(OllamaProviderConfig.class);
            OllamaProviderConfig ollama = (OllamaProviderConfig) provider;
            assertThat(ollama.getName()).isEqualTo("Local Ollama");
            assertThat(ollama.getBaseUrl()).isEqualTo("http://localhost:11434");
            assertThat(ollama.getModel()).isEqualTo("llama3.2:1b");
        });
    }

    @Test
    void openAiExampleUsesOnlyNonUsableLiteralCredential() throws IOException {
        Path example = examplePath("codegeist.openai.yml");
        CodegeistConfig config = configService.loadConfig(example.toString());

        assertThat(config.rootElements).singleElement().isInstanceOf(ProvidersRootElement.class);
        assertThat(config.defaultProvider()).hasValueSatisfying(provider -> {
            assertThat(provider).isInstanceOf(OpenAiProviderConfig.class);
            OpenAiProviderConfig openAi = (OpenAiProviderConfig) provider;
            assertThat(openAi.getName()).isEqualTo("OpenAI");
            assertThat(openAi.getApiKey()).isEqualTo(OPENAI_API_KEY_PLACEHOLDER).doesNotStartWith("sk-");
        });
        assertThat(Files.readString(example))
                .contains("api-key: " + OPENAI_API_KEY_PLACEHOLDER)
                .doesNotContain("#{", "getenv", "OPENAI_API_KEY", "sk-");
    }

    @Test
    void mcpExampleParsesExpectedStdioClientWithoutOpeningIt() {
        CodegeistConfig config = loadExample("codegeist.mcp.yml");

        assertThat(config.rootElements).singleElement().isInstanceOf(McpClientsRootElement.class);
        McpClientsConfig clients = config.rootElement(McpClientsRootElement.class).orElseThrow().getConfig();
        assertThat(clients.getElements()).singleElement().satisfies(client -> {
            assertThat(client).isInstanceOf(StdioMcpClientConfig.class);
            StdioMcpClientConfig stdio = (StdioMcpClientConfig) client;
            assertThat(stdio.getId()).isEqualTo("filesystem");
            assertThat(stdio.getType()).isEqualTo(McpClientConfig.Type.stdio);
            assertThat(stdio.getCommand()).isEqualTo("npx");
            assertThat(stdio.getArgs()).containsExactly("-y", "@modelcontextprotocol/server-filesystem", ".");
        });
    }

    private CodegeistConfig loadExample(String fileName) {
        return configService.loadConfig(examplePath(fileName).toString());
    }

    private Path examplePath(String fileName) {
        Path example = EXAMPLES_DIRECTORY.resolve(fileName);

        assertThat(example).isRegularFile();
        return example;
    }
}
