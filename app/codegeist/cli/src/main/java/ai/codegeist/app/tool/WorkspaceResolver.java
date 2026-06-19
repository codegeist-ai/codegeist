package ai.codegeist.app.tool;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.WorkspaceConfig;
import ai.codegeist.app.config.WorkspaceRootElement;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Resolves the workspace directory used by file and tool workflows.
 *
 * <p>The current T007 slice treats the workspace as an execution location, not as
 * a permission boundary. A direct {@code codegeist.yml} {@code workspace.directory}
 * value may be absolute or relative to the process working directory. The resolver
 * normalizes paths but intentionally does not reject filesystem roots, traversal
 * that normalizes inside or outside the process directory, symlinks, missing paths,
 * or session-store locations; those policies are deferred until a dedicated
 * workspace safety task exists.
 */
@Component
@RequiredArgsConstructor
public class WorkspaceResolver {

    private final CodegeistConfig config;

    @Value("${user.dir}")
    String workingDir;

    /**
     * Returns the active workspace for the injected config and process working
     * directory.
     *
     * <p>If no direct workspace root exists, or if {@code workspace.directory} is
     * blank, this method falls back to the normalized process working directory.
     */
    public Path currentWorkspace() {
        Path processWorkingDirectory = Path.of(workingDir).toAbsolutePath().normalize();
        return config.rootElement(WorkspaceRootElement.class)
                .map(WorkspaceRootElement::getConfig)
                .map(WorkspaceConfig::getDirectory)
                .filter(StringUtils::hasText)
                .map(directory -> resolveWorkspace(processWorkingDirectory, directory))
                .orElse(processWorkingDirectory);
    }

    private Path resolveWorkspace(Path processWorkingDirectory, String directory) {
        Path configuredDirectory = Path.of(directory);
        if (configuredDirectory.isAbsolute()) {
            return configuredDirectory.toAbsolutePath().normalize();
        }
        return processWorkingDirectory.resolve(configuredDirectory).normalize();
    }
}
