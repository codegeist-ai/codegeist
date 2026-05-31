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
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    args = CodegeistConfigService.SHOW_CONFIG_COMMAND
)
@ActiveProfiles("codegeist-config-service-test")
class CodegeistConfigCommandTest {

    private static final String PROVIDER_ID = "ollama";
    private static final String PROVIDER_NAME = "Ollama";

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Test
    void showConfigCommandPrintsMergedCodegeistYaml(CapturedOutput output) throws IOException {
        String stdout = output.getOut();
        Map<?, ?> yaml = yamlMapper.readValue(stdout, Map.class);

        assertThat(output.getErr()).isEmpty();
        assertThat(yaml).hasSize(1);
        assertThat(yaml.get("provider")).isInstanceOf(Map.class);

        Map<?, ?> provider = (Map<?, ?>) yaml.get("provider");
        assertThat(provider).hasSize(1);
        assertThat(provider.get(PROVIDER_ID)).isInstanceOf(Map.class);

        Map<?, ?> ollama = (Map<?, ?>) provider.get(PROVIDER_ID);
        assertThat(ollama.get("name")).isEqualTo(PROVIDER_NAME);
    }
}
