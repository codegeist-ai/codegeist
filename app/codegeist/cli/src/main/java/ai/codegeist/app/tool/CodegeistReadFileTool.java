package ai.codegeist.app.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
final class CodegeistReadFileTool implements CodegeistLocalTool {

    static final String TOOL_NAME = "codegeist_read";

    private final CodegeistFileToolSupport support;

    @Override
    public ToolDefinition definition() {
        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description("Read bounded line-numbered text from a file using the configured workspace encoding")
                .inputSchema(support.schema("""
                    "path":{"type":"string","description":"File path"},
                    "offset":{"type":"integer","description":"1-based starting line"},
                    "limit":{"type":"integer","description":"Maximum lines to return"}
                    """, CodegeistFileToolSupport.PATH_FIELD))
                .build();
    }

    @Override
    public CodegeistToolResult execute(CodegeistToolInput toolInput) {
        Path workspace = support.currentWorkspace();
        ReadToolInput input = support.parseInput(toolInput, ReadToolInput.class);
        Path file = support.resolvePath(workspace,
                support.requireText(input.path(), CodegeistFileToolSupport.REQUIRED_PATH_MESSAGE));
        support.requireExists(file, workspace);
        support.requireRegularFile(file, workspace);
        support.rejectBinaryFile(file, workspace);
        int offset = input.offset() == null || input.offset() <= 0 ? 1 : input.offset();
        int limit = support.outputBounds().cappedReadLimit(input.limit());

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = support.textReader(file)) {
            String line;
            int lineNumber = 1;
            while (lineNumber < offset && (line = reader.readLine()) != null) {
                support.rejectNulLine(line, file, workspace);
                lineNumber++;
            }
            while (lines.size() < limit && (line = reader.readLine()) != null) {
                support.rejectNulLine(line, file, workspace);
                lines.add(lineNumber + ": " + support.outputBounds().linePreview(line));
                lineNumber++;
            }
        }
        catch (CharacterCodingException exception) {
            throw new CodegeistToolException(
                    "File is not text in " + support.charset().displayName() + ": "
                            + support.displayPath(workspace, file), exception);
        }
        catch (IOException exception) {
            throw new CodegeistToolException("Failed to read file: " + support.displayPath(workspace, file), exception);
        }

        return new CodegeistToolResult(support.outputBounds().preview(String.join(CodegeistFileToolSupport.LINE_SEPARATOR, lines)));
    }

    private record ReadToolInput(String path, Integer offset, Integer limit) {
    }
}
