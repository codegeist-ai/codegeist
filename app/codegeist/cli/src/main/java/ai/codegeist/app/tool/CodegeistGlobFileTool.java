package ai.codegeist.app.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
final class CodegeistGlobFileTool implements CodegeistLocalTool {

    static final String TOOL_NAME = "codegeist_glob";

    private final CodegeistFileToolSupport support;

    @Override
    public ToolDefinition definition() {
        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description("Match bounded file or directory paths with a Java glob")
                .inputSchema(support.schema("""
                    "pattern":{"type":"string","description":"Java glob pattern"},
                    "path":{"type":"string","description":"Base directory"},
                    "limit":{"type":"integer","description":"Maximum matches to return"}
                    """, CodegeistFileToolSupport.PATTERN_FIELD))
                .build();
    }

    @Override
    public CodegeistToolResult execute(CodegeistToolInput toolInput) {
        Path workspace = support.currentWorkspace();
        GlobToolInput input = support.parseInput(toolInput, GlobToolInput.class);
        String pattern = support.requireText(input.pattern(), CodegeistFileToolSupport.REQUIRED_PATTERN_MESSAGE);
        Path baseDirectory = support.resolvePath(workspace, support.defaultPath(input.path()));
        support.requireExists(baseDirectory, workspace);
        if (!Files.isDirectory(baseDirectory, CodegeistFileToolSupport.NO_FOLLOW_LINKS)) {
            throw new CodegeistToolException(
                    "Path is not a directory: " + support.displayPath(workspace, baseDirectory));
        }
        int limit = support.outputBounds().cappedResultLimit(input.limit());
        PathMatcher matcher = support.pathMatcher(pattern);

        try (Stream<Path> paths = Files.walk(baseDirectory)) {
            List<String> matches = paths
                    .filter(path -> !path.equals(baseDirectory))
                    .filter(path -> support.matchesGlob(pattern, matcher, baseDirectory.relativize(path)))
                    .sorted(Comparator.comparing(path -> support.displayPath(workspace, path)))
                    .limit(limit)
                    .map(path -> support.displayPath(workspace, path))
                    .toList();
            String result = String.join(CodegeistFileToolSupport.LINE_SEPARATOR, matches);
            return new CodegeistToolResult(support.outputBounds().preview(result));
        }
        catch (IOException exception) {
            throw new CodegeistToolException(
                    "Failed to glob paths under: " + support.displayPath(workspace, baseDirectory), exception);
        }
    }

    private record GlobToolInput(String pattern, String path, Integer limit) {
    }
}
