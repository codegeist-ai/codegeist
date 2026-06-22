package ai.codegeist.app.tool;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.WorkspaceConfig;
import ai.codegeist.app.config.WorkspaceRootElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared containment guard for side-effecting local tools.
 *
 * <p>The guard keeps mutating file tools inside the active workspace by default,
 * while {@code workspace.dir-guard-disabled: true} disables only that containment
 * check for local workflows that intentionally need external file mutation. File
 * existence and regular-file checks still run in both modes.
 */
@Component
@RequiredArgsConstructor
final class CodegeistWorkingDirectoryGuard {

    private final CodegeistConfig config;

    Path requireExistingRegularFile(Path workspace, Path candidate, String displayPath) {
        Path normalizedWorkspace = workspace.toAbsolutePath().normalize();
        Path normalizedCandidate = candidate.toAbsolutePath().normalize();

        if (!isDirGuardDisabled() && !normalizedCandidate.startsWith(normalizedWorkspace)) {
            throw new CodegeistToolException("Path escapes workspace: " + displayPath);
        }
        if (!Files.exists(normalizedCandidate, CodegeistFileToolSupport.NO_FOLLOW_LINKS)) {
            throw new CodegeistToolException("Path does not exist: " + displayPath);
        }

        try {
            Path realCandidate = normalizedCandidate.toRealPath();
            if (!isDirGuardDisabled() && !realCandidate.startsWith(normalizedWorkspace.toRealPath())) {
                throw new CodegeistToolException("Path escapes workspace: " + displayPath);
            }
            if (!Files.isRegularFile(realCandidate)) {
                throw new CodegeistToolException("Path is not a file: " + displayPath);
            }
        }
        catch (CodegeistToolException exception) {
            throw exception;
        }
        catch (IOException exception) {
            throw new CodegeistToolException("Failed to resolve file path: " + displayPath, exception);
        }

        return normalizedCandidate;
    }

    private boolean isDirGuardDisabled() {
        return config.rootElement(WorkspaceRootElement.class)
                .map(WorkspaceRootElement::getConfig)
                .map(WorkspaceConfig::getDirGuardDisabled)
                .map(Boolean::booleanValue)
                .orElse(false);
    }
}
