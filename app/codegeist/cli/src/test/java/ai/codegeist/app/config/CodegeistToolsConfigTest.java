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
