package ai.codegeist.app.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class CodegeistProviderConfigTest {

    private final CodegeistConfigYamlMapper yamlMapper = new CodegeistConfigYamlMapper();
    private final CodegeistConfigRootParser rootParser = new CodegeistConfigRootParser(yamlMapper);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @MethodSource("supportedProviders")
    void dispatchesSupportedProviderTypesThroughExplicitRegistry(String type,
            Class<? extends ProviderConfig> providerClass, String yamlBody, String expectedDefaultModel) {
        ProviderConfig provider = loadSingleProvider(yamlBody);

        assertThat(provider).isInstanceOf(providerClass);
        assertThat(provider.getType()).isEqualTo(type);
        assertThat(provider.defaultModel()).isEqualTo(expectedDefaultModel);
    }

    @Test
    void missingProviderTypeFailsWithoutFallingBackToProviderId() {
        assertThatThrownBy(() -> loadAndValidate("""
            provider:
              openai:
                api-key: test-key
            """))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining("type is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "anthropic", "azure-openai", "bedrock-converse", "deepseek", "docker-model-runner",
        "google-genai", "groq", "minimax", "mistral-ai", "moonshot", "nvidia", "opencode-go",
        "opencode-zen", "openrouter", "perplexity", "qianfan", "xai"
    })
    void unsupportedProviderTypesFail(String unsupportedType) {
        assertThatThrownBy(() -> loadAndValidate("""
            provider:
              unsupported:
                type: %s
                api-key: test-key
            """.formatted(unsupportedType)))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining("Unsupported provider entry type")
                .hasMessageContaining(unsupportedType);
    }

    @Test
    void defaultProviderReturnsFirstNonNullConfiguredProvider() {
        CodegeistConfig config = loadAndValidate("""
            provider:
              missing: null
              ollama:
                type: ollama
                base-url: http://localhost:11434
            """);
        ProvidersRootElement rootElement = providers(config);

        assertThat(rootElement.getConfig().defaultProvider()).hasValueSatisfying(provider -> assertThat(provider)
                .isInstanceOf(OllamaProviderConfig.class));
        assertThat(config.defaultProvider()).hasValueSatisfying(provider -> assertThat(provider)
                .isInstanceOf(OllamaProviderConfig.class));
    }

    @Test
    void defaultProviderFailsWhenNoProviderIsConfigured() {
        CodegeistConfig config = loadAndValidate("""
            provider: {}
            """);
        ProvidersRootElement rootElement = providers(config);

        assertThat(rootElement.getConfig().defaultProvider()).isEmpty();
        assertThat(config.defaultProvider()).isEmpty();
    }

    @Test
    void configuredOllamaModelOverridesFallback() {
        OllamaProviderConfig ollama = (OllamaProviderConfig) loadSingleProvider("""
            type: ollama
            base-url: http://localhost:11434
            model: qwen3:4b-instruct
            """);

        assertThat(ollama.getModel()).isEqualTo("qwen3:4b-instruct");
        assertThat(ollama.defaultModel()).isEqualTo("qwen3:4b-instruct");
    }

    @ParameterizedTest
    @MethodSource("invalidProviderConfigs")
    void validatesRequiredProviderFields(String yamlBody, String expectedMessage) {
        assertThatThrownBy(() -> loadSingleProvider(yamlBody))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining(expectedMessage);
    }

    private static Stream<Arguments> supportedProviders() {
        return Stream.of(
                Arguments.of("ollama", OllamaProviderConfig.class, """
                    type: ollama
                    base-url: http://localhost:11434
                    """, OllamaProviderConfig.DEFAULT_MODEL),
                Arguments.of("openai", OpenAiProviderConfig.class, """
                    type: openai
                    api-key: test-key
                    organization-id: org-test
                    project-id: proj-test
                    """, OpenAiProviderConfig.DEFAULT_MODEL));
    }

    private static Stream<Arguments> invalidProviderConfigs() {
        return Stream.of(
                Arguments.of("""
                    type: openai
                    """, "apiKey"),
                Arguments.of("""
                    type: ollama
                    """, "base-url"),
                Arguments.of("""
                    type: ollama
                    base-url: http://localhost:11434
                    model: "   "
                    """, "model must not be blank when set"));
    }

    private ProviderConfig loadSingleProvider(String providerBody) {
        CodegeistConfig config = loadAndValidate("provider:\n  configured:\n" + indent(providerBody));
        return providers(config).getConfig().defaultProvider().orElseThrow();
    }

    private CodegeistConfig loadAndValidate(String yaml) {
        CodegeistConfig config = readConfig(yaml);
        Set<ConstraintViolation<CodegeistConfig>> violations = validator.validate(config);
        if (violations.isEmpty()) {
            return config;
        }

        String message = violations.stream()
                .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining("; "));
        throw new CodegeistConfigValidationException(message);
    }

    @SneakyThrows(JsonProcessingException.class)
    private CodegeistConfig readConfig(String yaml) {
        JsonNode root = yamlMapper.readTree(yaml);
        CodegeistConfig config = new CodegeistConfig();
        config.rootElements.add(rootParser.parseRootElement(ProvidersRootElement.ROOT_NAME,
                root.get(ProvidersRootElement.ROOT_NAME)));
        return config;
    }

    private String indent(String yamlBody) {
        return yamlBody.indent(4);
    }

    private ProvidersRootElement providers(CodegeistConfig config) {
        return config.rootElement(ProvidersRootElement.class).orElseThrow();
    }

}
