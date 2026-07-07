package ai.codegeist.app.chat;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistApplication;
import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistConfigService;
import ai.codegeist.app.config.OllamaProviderConfig;
import ai.codegeist.app.config.ProviderConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

class LocalOllamaProviderIT {

    private static final String OLLAMA_BASE_URL = "http://localhost:11434";
    private static final String OLLAMA_MODEL = "llama3.2:1b";
    private static final String PROMPT = "Respond with exactly the lowercase word codegeist. No explanation.";

    @TempDir
    private Path tempDir;

    @Test
    void chatsWithLocalOllamaThroughProviderNeutralService() throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(CodegeistApplication.class)
                .web(WebApplicationType.NONE)
                .run()) {
            CodegeistConfigService configService = context.getBean(CodegeistConfigService.class);
            CodegeistChatService chatService = context.getBean(CodegeistChatService.class);

            CodegeistConfig config = configService.loadConfig(writeConfig().toString());
            ProviderConfig providerConfig = config.defaultProvider().orElseThrow();
            assertThat(providerConfig).isInstanceOf(OllamaProviderConfig.class);

            CodegeistChatResponse response = chatService.chat(providerConfig, new CodegeistChatRequest(OLLAMA_MODEL, PROMPT));

            assertThat(response.content()).containsIgnoringCase("codegeist");
        }
    }

    private Path writeConfig() throws Exception {
        Path configFile = tempDir.resolve("codegeist.yml");
        Files.writeString(configFile, """
            provider:
              ollama:
                type: ollama
                base-url: %s
            """.formatted(OLLAMA_BASE_URL));
        return configFile;
    }

}
