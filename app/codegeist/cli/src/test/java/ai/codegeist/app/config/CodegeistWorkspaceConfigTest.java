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
class CodegeistWorkspaceConfigTest {

    private static final String CONFIG_FILE_NAME = "codegeist.yml";

    @Autowired
    private CodegeistConfigService service;

    @TempDir
    private Path tempDir;

    @Test
    void loadsWorkspaceDirectoryFromDirectYaml() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            workspace:
              directory: /tmp/codegeist-workspace
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());

        WorkspaceRootElement workspace = workspace(config);
        assertThat(workspace.getConfig().getDirectory()).isEqualTo("/tmp/codegeist-workspace");
    }

    @Test
    void omittedWorkspaceRootIsAllowed() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, "{}\n");

        CodegeistConfig config = service.loadConfig(configFile.toString());

        assertThat(config.rootElement(WorkspaceRootElement.class)).isEmpty();
    }

    @Test
    void blankWorkspaceDirectoryFallsBackAtResolutionBoundary() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, """
            workspace:
              directory: "   "
            """);

        CodegeistConfig config = service.loadConfig(configFile.toString());

        assertThat(workspace(config).getConfig().getDirectory()).isEqualTo("   ");
    }

    @Test
    void nonObjectWorkspaceRootFailsClearly() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        Files.writeString(configFile, "workspace: true\n");

        assertThatThrownBy(() -> service.loadConfig(configFile.toString()))
                .isInstanceOf(CodegeistConfigValidationException.class)
                .hasMessage(CodegeistConfigRootParser.rootObjectMessage(WorkspaceRootElement.ROOT_NAME));
    }

    private WorkspaceRootElement workspace(CodegeistConfig config) {
        return config.rootElement(WorkspaceRootElement.class).orElseThrow();
    }
}
