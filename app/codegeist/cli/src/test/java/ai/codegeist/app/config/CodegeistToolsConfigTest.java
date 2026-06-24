package ai.codegeist.app.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CodegeistToolsConfigTest {

    private static final String CONFIG_FILE_NAME = "codegeist.yml";

    @Autowired
    private CodegeistConfigService service;

    @TempDir
    private Path tempDir;

    @Test
    void loadsEditDiffPreviewSettingsFromDirectYaml() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            tools:
              codegeist-edit:
                diff-preview-lines: 2
                diff-preview-chars: 120
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());

        CodegeistEditToolConfig editToolConfig = tools(config).getConfig().getCodegeistEdit();
        assertThat(editToolConfig.getDiffPreviewLines()).isEqualTo(2);
        assertThat(editToolConfig.getDiffPreviewChars()).isEqualTo(120);
        assertThat(service.toYaml(config))
                .contains("tools:", "codegeist-edit:", "diff-preview-lines: 2", "diff-preview-chars: 120");
    }

    @Test
    void omittedEditToolConfigIsAllowed() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, "tools: {}\n");

        CodegeistConfig config = service.loadConfig(configFile.toString());

        assertThat(tools(config).getConfig().getCodegeistEdit()).isNull();
        assertThat(tools(config).getConfig().getCodegeistShell()).isNull();
    }

    @Test
    void loadsShellSettingsFromDirectYaml() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            tools:
              codegeist-shell:
                default-timeout-seconds: 90
                command-prefix:
                  - docker
                  - run
                  - --rm
                  - ubuntu
                  - bash
                  - -lc
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());

        CodegeistShellToolConfig shellToolConfig = tools(config).getConfig().getCodegeistShell();
        assertThat(shellToolConfig.getDefaultTimeoutSeconds()).isEqualTo(90);
        assertThat(shellToolConfig.getCommandPrefix()).containsExactly("docker", "run", "--rm", "ubuntu", "bash", "-lc");
        assertThat(service.toYaml(config))
                .contains("tools:", "codegeist-shell:", "default-timeout-seconds: 90", "command-prefix:",
                        "- \"docker\"", "- \"run\"", "- \"-lc\"");
    }

    @Test
    void invalidShellSettingsFailThroughBeanValidation() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            tools:
              codegeist-shell:
                default-timeout-seconds: 0
                command-prefix:
                  - ""
            """);

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessageContaining("defaultTimeoutSeconds")
                .hasMessageContaining("must be greater than 0")
                .hasMessageContaining("commandPrefix")
                .hasMessageContaining("must not be blank");
    }

    @Test
    void nonObjectToolsRootFailsClearly() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, "tools: true\n");

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessage(CodegeistConfigRootParser.rootObjectMessage(ToolsRootElement.ROOT_NAME));
    }

    private ToolsRootElement tools(CodegeistConfig config) {
        return config.rootElement(ToolsRootElement.class).orElseThrow();
    }
}
