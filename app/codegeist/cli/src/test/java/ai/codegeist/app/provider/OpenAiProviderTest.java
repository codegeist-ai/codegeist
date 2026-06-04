package ai.codegeist.app.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistConfigService;
import ai.codegeist.app.config.CodegeistConfigValidationException;
import ai.codegeist.app.config.OpenAiProviderConfig;
import ai.codegeist.app.config.ProviderConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OpenAiProviderTest {

    private static final String PROVIDER_ID = "openai";
    private static final String DEFAULT_BASE_URL = "https://api.openai.com";
    private static final String OPENAI_TEST_API_KEY_ENV = "CODEGEIST_TEST_OPENAI_APIKEY";
    private static final String OPENAI_BASE_URL_ENV = "CODEGEIST_TEST_OPENAI_BASE_URL";
    private static final String IMAGE_MODEL_ENV = "CODEGEIST_TEST_OPENAI_IMAGE_MODEL";
    private static final String IMAGE_SIZE_ENV = "CODEGEIST_TEST_OPENAI_IMAGE_SIZE";
    private static final String SPEECH_MODEL_ENV = "CODEGEIST_TEST_OPENAI_SPEECH_MODEL";
    private static final String SPEECH_TO_TEXT_MODEL_ENV = "CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_MODEL";
    private static final String SPEECH_TO_TEXT_AUDIO_ENV = "CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_AUDIO";
    private static final String SPEECH_TO_TEXT_EXPECTED_ENV = "CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_EXPECTED";
    private static final String DEFAULT_IMAGE_MODEL = "gpt-image-1-mini";
    private static final String DEFAULT_IMAGE_SIZE = "1024x1024";
    private static final String DEFAULT_SPEECH_MODEL = "tts-1";
    private static final String DEFAULT_SPEECH_TO_TEXT_MODEL = "gpt-4o-mini-transcribe";
    private static final String DEFAULT_SPEECH_TO_TEXT_EXPECTED = "codegeist";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private CodegeistConfigService configService;

    @TempDir
    private Path tempDir;

    @Test
    void testProviderConfig() throws Exception {
        CodegeistConfig config = loadConfig("""
            provider:
              openai:
                type: openai
                api-key: local-openai-key
                organization-id: org-local
                project-id: project-local
            """);

        ProviderConfig provider = config.getProvider().get(PROVIDER_ID);
        assertThat(provider).isInstanceOf(OpenAiProviderConfig.class);
        OpenAiProviderConfig openai = (OpenAiProviderConfig) provider;
        assertThat(openai.getApiKey()).isEqualTo("local-openai-key");
        assertThat(openai.getOrganizationId()).isEqualTo("org-local");
        assertThat(openai.getProjectId()).isEqualTo("project-local");
    }

    @Test
    void testMissingApiKeyFailsConfig() {
        assertThatThrownBy(() -> loadConfig("""
            provider:
              openai:
                type: openai
            """))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining("apiKey")
                .hasMessageContaining("must not be blank");
    }

    @Test
    @ProviderCategory(ProviderTestCategory.remote_free)
    void testListModels() throws Exception {
        HttpResponse<String> response = sendJson(requestBuilder("/v1/models").GET().build());
        assertSuccess(response.statusCode(), response.body());
        JsonNode body = jsonMapper.readTree(response.body());
        assertThat(body.path("data").isArray()).isTrue();
        assertThat(body.path("data").size()).isGreaterThan(0);
    }

    @Test
    @ProviderCategory(ProviderTestCategory.remote_paid)
    void testImageGeneration() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "model", envOrDefault(IMAGE_MODEL_ENV, DEFAULT_IMAGE_MODEL),
                "prompt", "Create a simple test image containing the word codegeist.",
                "size", envOrDefault(IMAGE_SIZE_ENV, DEFAULT_IMAGE_SIZE),
                "n", 1);

        HttpResponse<String> response = postJson("/v1/images/generations", requestBody);
        assertSuccess(response.statusCode(), response.body());
        JsonNode body = jsonMapper.readTree(response.body());
        assertThat(body.path("data").isArray()).isTrue();
        assertThat(body.path("data").size()).isGreaterThan(0);
    }

    @Test
    @ProviderCategory(ProviderTestCategory.remote_paid)
    void testTextToSpeech() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "model", envOrDefault(SPEECH_MODEL_ENV, DEFAULT_SPEECH_MODEL),
                "input", "Codegeist provider test.",
                "voice", "alloy");

        HttpRequest request = requestBuilder("/v1/audio/speech")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonMapper.writeValueAsString(requestBody)))
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        assertSuccess(response.statusCode(), new String(response.body(), StandardCharsets.UTF_8));
        assertThat(response.body().length).isGreaterThan(100);
    }

    @Test
    @ProviderCategory(ProviderTestCategory.remote_paid)
    void testSpeechToText() throws Exception {
        Path audioPath = Path.of(requireEnv(SPEECH_TO_TEXT_AUDIO_ENV));
        assumeTrue(Files.isRegularFile(audioPath), () -> "Missing audio fixture: " + audioPath);
        String expectedText = envOrDefault(SPEECH_TO_TEXT_EXPECTED_ENV, DEFAULT_SPEECH_TO_TEXT_EXPECTED);
        MultipartBody multipartBody = multipartBody(Map.of(
                "model", envOrDefault(SPEECH_TO_TEXT_MODEL_ENV, DEFAULT_SPEECH_TO_TEXT_MODEL),
                "response_format", "json"), audioPath);

        HttpRequest request = requestBuilder("/v1/audio/transcriptions")
                .header("Content-Type", "multipart/form-data; boundary=" + multipartBody.boundary())
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody.bytes()))
                .build();

        HttpResponse<String> response = sendJson(request);
        assertSuccess(response.statusCode(), response.body());
        JsonNode body = jsonMapper.readTree(response.body());
        assertThat(body.path("text").asText()).containsIgnoringCase(expectedText);
    }

    private CodegeistConfig loadConfig(String yaml) throws Exception {
        Path configFile = tempDir.resolve("codegeist.yml");
        Files.writeString(configFile, yaml);
        return configService.loadConfig(configFile.toString());
    }

    private HttpResponse<String> postJson(String path, Map<String, Object> requestBody) throws Exception {
        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonMapper.writeValueAsString(requestBody)))
                .build();
        return sendJson(request);
    }

    private HttpResponse<String> sendJson(HttpRequest request) throws Exception {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpRequest.Builder requestBuilder(String path) {
        return HttpRequest.newBuilder(endpoint(path))
                .header("Authorization", "Bearer " + requireEnv(OPENAI_TEST_API_KEY_ENV));
    }

    private URI endpoint(String path) {
        String baseUrl = envOrDefault(OPENAI_BASE_URL_ENV, DEFAULT_BASE_URL);
        return URI.create(baseUrl.replaceAll("/+$", "") + path);
    }

    private String requireEnv(String envName) {
        String value = System.getenv(envName);
        assumeTrue(StringUtils.hasText(value), () -> "Missing provider test environment variable: " + envName);
        return value;
    }

    private String envOrDefault(String envName, String defaultValue) {
        String value = System.getenv(envName);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return value;
    }

    private MultipartBody multipartBody(Map<String, String> fields, Path filePath) throws Exception {
        String boundary = "codegeist-" + UUID.randomUUID();
        List<byte[]> parts = new ArrayList<>();
        for (Map.Entry<String, String> field : fields.entrySet()) {
            parts.add(("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"" + field.getKey() + "\"\r\n\r\n"
                    + field.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
        parts.add(("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath.getFileName() + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        parts.add(Files.readAllBytes(filePath));
        parts.add(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        int length = parts.stream().mapToInt(part -> part.length).sum();
        byte[] bytes = new byte[length];
        int offset = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, bytes, offset, part.length);
            offset += part.length;
        }
        return new MultipartBody(boundary, bytes);
    }

    private void assertSuccess(int statusCode, String body) {
        assertThat(statusCode)
                .as("OpenAI response status with body: %s", body)
                .isBetween(200, 299);
    }

    private record MultipartBody(String boundary, byte[] bytes) {
    }
}
