package ai.codegeist.app.chat;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistApplication;
import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistConfigService;
import ai.codegeist.app.config.OllamaProviderConfig;
import ai.codegeist.app.config.ProviderConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

class LocalOllamaProviderIT {

    private static final String OLLAMA_PROVIDER_ID = "ollama";
    private static final String DEFAULT_OLLAMA_BASE_URL = "http://localhost:11434";
    private static final String DEFAULT_OLLAMA_MODEL = "llama3.2:1b";
    private static final String OLLAMA_BASE_URL_ENV = "CODEGEIST_TEST_OLLAMA_BASE_URL";
    private static final String OLLAMA_MODEL_ENV = "CODEGEIST_TEST_OLLAMA_MODEL";
    private static final String OLLAMA_MODEL_FALLBACK_ENV = "OLLAMA_MODEL";
    private static final String SPRING_SHELL_AUTO_CONFIGURATION =
            "org.springframework.shell.core.autoconfigure.SpringShellAutoConfiguration";
    private static final String PROMPT = "Respond with exactly the lowercase word codegeist. No explanation.";
    private static final int DETERMINISTIC_SEED = 7;

    @TempDir
    private Path tempDir;

    @Test
    void chatsWithLocalOllamaThroughProviderNeutralService() throws Exception {
        String baseUrl = environmentOrDefault(OLLAMA_BASE_URL_ENV, DEFAULT_OLLAMA_BASE_URL);
        String model = environmentOrDefault(OLLAMA_MODEL_ENV,
                environmentOrDefault(OLLAMA_MODEL_FALLBACK_ENV, DEFAULT_OLLAMA_MODEL));

        Instant springContextStart = Instant.now();
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(CodegeistApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.autoconfigure.exclude=" + SPRING_SHELL_AUTO_CONFIGURATION)
                .run()) {
            Duration springContextDuration = Duration.between(springContextStart, Instant.now());

            Instant readinessStart = Instant.now();
            assertModelIsAvailable(baseUrl, model);
            Duration readinessDuration = Duration.between(readinessStart, Instant.now());

            CodegeistConfigService configService = context.getBean(CodegeistConfigService.class);
            CodegeistChatService chatService = context.getBean(CodegeistChatService.class);

            CodegeistConfig config = configService.loadConfig(writeConfig(baseUrl, model).toString());
            ProviderConfig providerConfig = config.getProvider().get(OLLAMA_PROVIDER_ID);
            assertThat(providerConfig).isInstanceOf(OllamaProviderConfig.class);

            Instant chatStart = Instant.now();
            CodegeistChatResponse response = chatService.chat(new CodegeistChatRequest(providerConfig, PROMPT));
            Duration chatDuration = Duration.between(chatStart, Instant.now());

            System.out.println("Spring context startup duration: " + springContextDuration);
            System.out.println("Ollama readiness/model-availability duration: " + readinessDuration);
            System.out.println("Ollama first chat-call duration: " + chatDuration);

            assertThat(response.content()).containsIgnoringCase("codegeist");
        }
    }

    private void assertModelIsAvailable(String baseUrl, String model) {
        OllamaApi api = OllamaApi.builder()
                .baseUrl(baseUrl)
                .build();
        String normalizedModel = normalizeModelName(model);
        assertThat(api.listModels().models())
                .as("Ollama model %s must already be downloaded at %s", normalizedModel, baseUrl)
                .anySatisfy(availableModel -> assertThat(availableModel.name()).isEqualTo(normalizedModel));
    }

    private Path writeConfig(String baseUrl, String model) throws Exception {
        Path configFile = tempDir.resolve("codegeist.yml");
        Files.writeString(configFile, """
            provider:
              ollama:
                type: ollama
                enabled: true
                model: %s
                base-url: %s
                options:
                  temperature: 0
                  seed: %d
            """.formatted(model, baseUrl, DETERMINISTIC_SEED));
        return configFile;
    }

    private String normalizeModelName(String model) {
        String normalized = model.trim();
        if (normalized.contains(":")) {
            return normalized;
        }
        return normalized + ":latest";
    }

    private String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
