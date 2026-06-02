package ai.codegeist.app.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Arrays;
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

    private final ObjectMapper yamlMapper = new CodegeistYamlConfiguration().codegeistYamlObjectMapper();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @MethodSource("supportedProviders")
    void dispatchesSupportedProviderTypesThroughProviderAnnotation(String type,
            Class<? extends ProviderConfig> providerClass, String yamlBody) {
        ProviderConfig provider = loadSingleProvider(yamlBody);

        assertThat(provider).isInstanceOf(providerClass);
        assertThat(provider.getType()).isEqualTo(type);
        assertThat(provider.getClass().getAnnotation(Provider.class).value()).isEqualTo(type);
    }

    @Test
    void permittedProviderAnnotationsIncludeOnlyOllamaAndOpenAiProviderTypes() {
        assertThat(Arrays.stream(ProviderConfig.class.getPermittedSubclasses())
                .map(providerClass -> providerClass.getAnnotation(Provider.class).value()))
                .containsExactly("ollama", "openai");
    }

    @Test
    void missingProviderTypeFailsWithoutFallingBackToProviderId() {
        assertThatThrownBy(() -> loadAndValidate("""
            provider:
              openai:
                model: gpt-4o-mini
                api-key: test-key
            """))
                .isInstanceOf(JsonProcessingException.class)
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
                model: model-id
                api-key: test-key
            """.formatted(unsupportedType)))
                .isInstanceOf(JsonProcessingException.class)
                .hasMessageContaining("Unsupported provider type")
                .hasMessageContaining(unsupportedType);
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
                    model: llama3.2:1b
                    base-url: http://localhost:11434
                    options:
                      temperature: 0
                    """),
                Arguments.of("openai", OpenAiProviderConfig.class, """
                    type: openai
                    model: gpt-4o-mini
                    api-key: test-key
                    organization-id: org-test
                    project-id: proj-test
                    """));
    }

    private static Stream<Arguments> invalidProviderConfigs() {
        return Stream.of(
                Arguments.of("""
                    type: openai
                    api-key: test-key
                    """, "model"),
                Arguments.of("""
                    type: openai
                    model: gpt-4o-mini
                    """, "apiKey"),
                Arguments.of("""
                    type: ollama
                    model: llama3.2:1b
                    """, "base-url"));
    }

    private ProviderConfig loadSingleProvider(String providerBody) {
        CodegeistConfig config = loadAndValidate("provider:\n  configured:\n" + indent(providerBody));
        return config.getProvider().get("configured");
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
        return yamlMapper.readerFor(CodegeistConfig.class).readValue(yaml);
    }

    private String indent(String yamlBody) {
        return yamlBody.indent(4);
    }
}
