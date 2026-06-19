package ai.codegeist.app.tool;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistConfigElement;
import ai.codegeist.app.config.CodegeistConfigRootParser;
import ai.codegeist.app.config.CodegeistConfigRootElement;
import ai.codegeist.app.config.CodegeistConfigYamlMapper;
import ai.codegeist.app.config.WorkspaceRootElement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

class WorkspaceResolverTest {

    private final CodegeistConfigYamlMapper yamlMapper = new CodegeistConfigYamlMapper();
    private final CodegeistConfigRootParser rootParser = new CodegeistConfigRootParser(yamlMapper);

    @TempDir
    private Path tempDir;

    @Test
    void usesProcessWorkingDirectoryWhenConfigIsAbsent() {
        WorkspaceResolver resolver = resolver(new CodegeistConfig());

        assertThat(resolver.currentWorkspace()).isEqualTo(processWorkingDirectory());
    }

    @Test
    void usesProcessWorkingDirectoryWhenDirectoryIsBlank() {
        WorkspaceResolver resolver = resolver(configWithWorkspace("""
            workspace:
              directory: "   "
            """));

        assertThat(resolver.currentWorkspace()).isEqualTo(processWorkingDirectory());
    }

    @Test
    void resolvesAbsoluteWorkspaceOverride() {
        Path workspace = tempDir.resolve("absolute-workspace").toAbsolutePath().normalize();
        WorkspaceResolver resolver = resolver(configWithWorkspace("""
            workspace:
              directory: "%s"
            """.formatted(workspace)));

        assertThat(resolver.currentWorkspace()).isEqualTo(workspace);
    }

    @Test
    void resolvesRelativeWorkspaceOverrideAgainstProcessWorkingDirectory() {
        WorkspaceResolver resolver = resolver(configWithWorkspace("""
            workspace:
              directory: nested/../target-workspace
            """));

        assertThat(resolver.currentWorkspace()).isEqualTo(processWorkingDirectory().resolve("target-workspace"));
    }

    @Test
    void allowsFilesystemRootAsWorkspace() {
        Path filesystemRoot = processWorkingDirectory().getRoot();
        WorkspaceResolver resolver = resolver(configWithWorkspace("""
            workspace:
              directory: "%s"
            """.formatted(filesystemRoot)));

        assertThat(resolver.currentWorkspace()).isEqualTo(filesystemRoot.toAbsolutePath().normalize());
    }

    @Test
    void normalizesTraversalSegmentsInConfiguredWorkspace() {
        WorkspaceResolver resolver = resolver(configWithWorkspace("""
            workspace:
              directory: ../outside/../workspace
            """));

        assertThat(resolver.currentWorkspace()).isEqualTo(processWorkingDirectory().getParent().resolve("workspace"));
    }

    private WorkspaceResolver resolver(CodegeistConfig config) {
        WorkspaceResolver resolver = new WorkspaceResolver(config);
        ReflectionTestUtils.setField(resolver, "workingDir", tempDir.toString());
        return resolver;
    }

    private Path processWorkingDirectory() {
        return tempDir.toAbsolutePath().normalize();
    }

    private CodegeistConfig configWithWorkspace(String yaml) {
        CodegeistConfig config = new CodegeistConfig();
        addRootElement(config, parseWorkspace(yaml));
        return config;
    }

    @SuppressWarnings("unchecked")
    private void addRootElement(CodegeistConfig config,
            CodegeistConfigRootElement<? extends CodegeistConfigElement> rootElement) {
        List<CodegeistConfigRootElement<? extends CodegeistConfigElement>> rootElements =
                (List<CodegeistConfigRootElement<? extends CodegeistConfigElement>>) ReflectionTestUtils
                        .getField(config, "rootElements");
        rootElements.add(rootElement);
    }

    private WorkspaceRootElement parseWorkspace(String yaml) {
        JsonNode root = readTree(yaml);
        return (WorkspaceRootElement) rootParser.parseRootElement(WorkspaceRootElement.ROOT_NAME,
                root.get(WorkspaceRootElement.ROOT_NAME));
    }

    private JsonNode readTree(String yaml) {
        try {
            return yamlMapper.readTree(yaml);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}
