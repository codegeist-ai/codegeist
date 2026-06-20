package ai.codegeist.app.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
final class CodegeistWriteFileTool implements CodegeistLocalTool {

    static final String TOOL_NAME = "codegeist_write";

    private final CodegeistFileToolSupport support;

    @Override
    public ToolDefinition definition() {
        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description("Create or overwrite one regular text file using the configured workspace encoding")
                .inputSchema(support.schema("""
                    "path":{"type":"string","description":"File path"},
                    "content":{"type":"string","description":"Text content to write"}
                    """, CodegeistFileToolSupport.PATH_FIELD, CodegeistFileToolSupport.CONTENT_FIELD))
                .build();
    }

    @Override
    public CodegeistToolResult execute(CodegeistToolInput toolInput) {
        Path workspace = support.currentWorkspace();
        WriteToolInput input = support.parseInput(toolInput, WriteToolInput.class);
        Path file = support.resolvePath(workspace,
                support.requireText(input.path(), CodegeistFileToolSupport.REQUIRED_PATH_MESSAGE));
        if (input.content() == null) {
            throw new CodegeistToolException(CodegeistFileToolSupport.REQUIRED_CONTENT_MESSAGE);
        }
        if (Files.isDirectory(file, CodegeistFileToolSupport.NO_FOLLOW_LINKS)) {
            throw new CodegeistToolException("Path is a directory: " + support.displayPath(workspace, file));
        }
        Path parent = file.getParent();
        if (parent == null || !Files.isDirectory(parent, CodegeistFileToolSupport.NO_FOLLOW_LINKS)) {
            throw new CodegeistToolException(
                    "Parent directory does not exist: " + support.displayPath(workspace, file));
        }
        if (Files.exists(file, CodegeistFileToolSupport.NO_FOLLOW_LINKS)
                && !Files.isRegularFile(file, CodegeistFileToolSupport.NO_FOLLOW_LINKS)) {
            throw new CodegeistToolException("Path is not a regular file: " + support.displayPath(workspace, file));
        }

        boolean existed = Files.exists(file, CodegeistFileToolSupport.NO_FOLLOW_LINKS);
        try {
            Files.writeString(
                    file,
                    input.content(),
                    support.charset(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        }
        catch (IOException exception) {
            throw new CodegeistToolException("Failed to write file: " + support.displayPath(workspace, file), exception);
        }

        String action = existed ? "Overwrote" : "Created";
        return new CodegeistToolResult(support.outputBounds().preview(("%s file: %s" + CodegeistFileToolSupport.LINE_SEPARATOR
                + "Characters: %d")
                .formatted(action, support.displayPath(workspace, file), input.content().length())));
    }

    private record WriteToolInput(String path, String content) {
    }
}
