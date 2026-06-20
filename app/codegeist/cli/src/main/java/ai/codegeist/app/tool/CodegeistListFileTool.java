package ai.codegeist.app.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
final class CodegeistListFileTool implements CodegeistLocalTool {

    static final String TOOL_NAME = "codegeist_list";

    private final CodegeistFileToolSupport support;

    @Override
    public ToolDefinition definition() {
        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description("List bounded direct directory entries")
                .inputSchema(support.schema("""
                    "path":{"type":"string","description":"Directory path"},
                    "limit":{"type":"integer","description":"Maximum entries to return"}
                    """))
                .build();
    }

    @Override
    public CodegeistToolResult execute(CodegeistToolInput toolInput) {
        Path workspace = support.currentWorkspace();
        ListToolInput input = support.parseInput(toolInput, ListToolInput.class);
        Path directory = support.resolvePath(workspace, support.defaultPath(input.path()));
        support.requireExists(directory, workspace);
        if (!Files.isDirectory(directory, CodegeistFileToolSupport.NO_FOLLOW_LINKS)) {
            throw new CodegeistToolException("Path is not a directory: " + support.displayPath(workspace, directory));
        }
        int limit = support.outputBounds().cappedResultLimit(input.limit());

        try (Stream<Path> entries = Files.list(directory)) {
            List<String> renderedEntries = entries
                    .sorted(Comparator.comparing(path -> support.displayPath(workspace, path)))
                    .limit(limit)
                    .map(path -> support.entryPreview(workspace, path))
                    .toList();
            String result = String.join(CodegeistFileToolSupport.LINE_SEPARATOR, renderedEntries);
            return new CodegeistToolResult(support.outputBounds().preview(result));
        }
        catch (IOException exception) {
            throw new CodegeistToolException(
                    "Failed to list directory: " + support.displayPath(workspace, directory), exception);
        }
    }

    private record ListToolInput(String path, Integer limit) {
    }
}
