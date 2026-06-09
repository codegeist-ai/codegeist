package ai.codegeist.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    args = CodegeistConfigService.SHOW_CONFIG_COMMAND,
    properties = CodegeistConfigService.CONFIG_PROPERTY + "=src/test/resources/codegeist-current-config-test.yml"
)
class CodegeistConfigCommandTest {

    private static final String OPENAI_PROVIDER_ID = "openai";
    private static final String RAW_SECRET = "injected-openai-key";

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Test
    void showConfigCommandPrintsCurrentCodegeistYaml(CapturedOutput output) throws IOException {
        String stdout = output.getOut();
        Map<?, ?> yaml = yamlMapper.readValue(stdout, Map.class);

        assertThat(output.getErr()).isEmpty();
        assertThat(stdout).doesNotContain("---").doesNotContain("codegeist:").contains(RAW_SECRET);
        assertThat(yaml).hasSize(1);
        assertThat(yaml.containsKey("provider")).isTrue();
        assertThat(yaml.get("provider")).isInstanceOf(Map.class);

        Map<?, ?> provider = (Map<?, ?>) yaml.get("provider");
        assertThat(provider).hasSize(1);
        assertThat(provider.get(OPENAI_PROVIDER_ID)).isInstanceOf(Map.class);

        Map<?, ?> openai = (Map<?, ?>) provider.get(OPENAI_PROVIDER_ID);
        assertThat(openai.get("type")).isEqualTo(OPENAI_PROVIDER_ID);
        assertThat(openai.get("api-key")).isEqualTo(RAW_SECRET);
    }
}
