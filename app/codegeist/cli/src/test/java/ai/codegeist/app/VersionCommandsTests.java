package ai.codegeist.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    args = VersionCommands.VERSION_COMMAND
)
class VersionCommandsTests {

    @Autowired
    private BuildProperties buildProperties;

    @Test
    void versionCommandRunsThroughSpringContext(CapturedOutput output) {
        assertThat(output.getOut()).isEqualTo(buildProperties.getVersion());
        assertThat(output.getErr()).isEmpty();
    }
}
