package ai.codegeist.app.tool;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistConfigElement;
import ai.codegeist.app.config.CodegeistConfigRootElement;
import ai.codegeist.app.config.WorkspaceConfig;
import ai.codegeist.app.config.WorkspaceRootElement;
import ai.codegeist.app.session.ToolSessionPart;
import ai.codegeist.app.session.ToolSessionPart.ToolSessionPartStatus;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.test.util.ReflectionTestUtils;

class CodegeistLocalToolsTest {

    private static final String LINE_SEPARATOR = CodegeistFileToolSupport.LINE_SEPARATOR;

    @TempDir
    private Path tempDir;

    @Test
    void exposesLocalCallbacksByName() {
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        List<ToolCallback> callbacks = localTools().callbacks(recordedParts::add);

        assertThat(callbacks)
                .extracting(callback -> callback.getToolDefinition().name())
                .containsExactlyInAnyOrder(
                        CodegeistReadFileTool.TOOL_NAME,
                        CodegeistListFileTool.TOOL_NAME,
                        CodegeistGlobFileTool.TOOL_NAME,
                        CodegeistGrepFileTool.TOOL_NAME,
                        CodegeistWriteFileTool.TOOL_NAME);
        assertThat(callbacks)
                .allSatisfy(callback -> {
                    assertThat(callback.getToolMetadata().returnDirect()).isFalse();
                    assertThat(callback.getToolDefinition().inputSchema()).contains("type", "object");
                });
    }

    @Test
    void readReturnsBoundedLineNumberedTextAndRecordsToolPart() throws IOException {
        Files.writeString(tempDir.resolve("notes.txt"), "alpha\nbeta\ngamma\n");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistReadFileTool.TOOL_NAME,
                """
                    {"path":"notes.txt","offset":2,"limit":2}
                    """,
                recordedParts);

        assertThat(output).isEqualTo("2: beta" + LINE_SEPARATOR + "3: gamma");
        assertCompletedPart(recordedParts, CodegeistReadFileTool.TOOL_NAME, output);
    }

    @Test
    void readRejectsMissingDirectoriesAndBinaryFiles() throws IOException {
        Files.createDirectory(tempDir.resolve("directory"));
        Files.write(tempDir.resolve("binary.dat"), new byte[] {'a', 0, 'b'});
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String missingOutput = callTool(CodegeistReadFileTool.TOOL_NAME,
                """
                    {"path":"missing.txt"}
                    """,
                recordedParts);
        String directoryOutput = callTool(CodegeistReadFileTool.TOOL_NAME,
                """
                    {"path":"directory"}
                    """,
                recordedParts);
        String binaryOutput = callTool(CodegeistReadFileTool.TOOL_NAME,
                """
                    {"path":"binary.dat"}
                    """,
                recordedParts);

        assertThat(missingOutput).contains("Path does not exist");
        assertThat(directoryOutput).contains("Path is not a file");
        assertThat(binaryOutput).contains("File is not text");
        assertThat(recordedParts)
                .hasSize(3)
                .allSatisfy(part -> assertThat(part.getStatus()).isEqualTo(ToolSessionPartStatus.failed));
    }

    @Test
    void readUsesConfiguredWorkspaceEncoding() throws IOException {
        Files.write(tempDir.resolve("latin1.txt"), "Gr\u00fc\u00dfe".getBytes(StandardCharsets.ISO_8859_1));
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistReadFileTool.TOOL_NAME,
                """
                    {"path":"latin1.txt","limit":1}
                    """,
                recordedParts,
                "ISO-8859-1");

        assertThat(output).isEqualTo("1: Gr\u00fc\u00dfe");
        assertCompletedPart(recordedParts, CodegeistReadFileTool.TOOL_NAME, output);
    }

    @Test
    void listReturnsStableDirectEntriesAndRejectsFiles() throws IOException {
        Files.createDirectory(tempDir.resolve("docs"));
        Files.writeString(tempDir.resolve("alpha.txt"), "alpha");
        Files.writeString(tempDir.resolve("zeta.txt"), "zeta");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistListFileTool.TOOL_NAME,
                """
                    {"path":".","limit":2}
                    """,
                recordedParts);
        String fileOutput = callTool(CodegeistListFileTool.TOOL_NAME,
                """
                    {"path":"alpha.txt"}
                    """,
                recordedParts);

        assertThat(output).isEqualTo("[FILE] alpha.txt" + LINE_SEPARATOR + "[DIR] docs/");
        assertThat(fileOutput).contains("Path is not a directory");
        assertCompletedPart(recordedParts.subList(0, 1), CodegeistListFileTool.TOOL_NAME, output);
        assertThat(recordedParts.get(1).getStatus()).isEqualTo(ToolSessionPartStatus.failed);
    }

    @Test
    void globReturnsSortedBoundedWorkspaceRelativeMatches() throws IOException {
        Files.createDirectories(tempDir.resolve("src/internal"));
        Files.writeString(tempDir.resolve("src/App.java"), "class App {}");
        Files.writeString(tempDir.resolve("src/internal/Util.java"), "class Util {}");
        Files.writeString(tempDir.resolve("src/readme.txt"), "text");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistGlobFileTool.TOOL_NAME,
                """
                    {"path":".","pattern":"**/*.java","limit":1}
                    """,
                recordedParts);

        assertThat(output).isEqualTo("src/App.java");
        assertCompletedPart(recordedParts, CodegeistGlobFileTool.TOOL_NAME, output);
    }

    @Test
    void grepReturnsSortedLinePreviewsAndRecordsInvalidRegexFailure() throws IOException {
        Files.createDirectories(tempDir.resolve("src"));
        Files.writeString(tempDir.resolve("src/App.java"), "class App {}\nno match\n");
        Files.writeString(tempDir.resolve("src/Second.java"), "class Second {}\n");
        Files.writeString(tempDir.resolve("src/binary.dat"), new String(new char[] {'a', 0, 'b'}));
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistGrepFileTool.TOOL_NAME,
                """
                    {"path":"src","include":"**/*.java","pattern":"class .*","limit":2}
                    """,
                recordedParts);
        String invalidRegexOutput = callTool(CodegeistGrepFileTool.TOOL_NAME,
                """
                    {"path":"src","pattern":"["}
                    """,
                recordedParts);

        assertThat(output).isEqualTo("src/App.java:1: class App {}"
                + LINE_SEPARATOR
                + "src/Second.java:1: class Second {}");
        assertThat(invalidRegexOutput).contains("Invalid regex");
        assertCompletedPart(recordedParts.subList(0, 1), CodegeistGrepFileTool.TOOL_NAME, output);
        assertThat(recordedParts.get(1).getStatus()).isEqualTo(ToolSessionPartStatus.failed);
        assertThat(recordedParts.get(1).getOutputPreview()).isEqualTo(invalidRegexOutput);
    }

    @Test
    void writeCreatesAndOverwritesRegularTextFiles() throws IOException {
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String createdOutput = callTool(CodegeistWriteFileTool.TOOL_NAME,
                """
                    {"path":"created.txt","content":"hello"}
                    """,
                recordedParts);
        String overwrittenOutput = callTool(CodegeistWriteFileTool.TOOL_NAME,
                """
                    {"path":"created.txt","content":"hello again"}
                    """,
                recordedParts);

        assertThat(Files.readString(tempDir.resolve("created.txt"))).isEqualTo("hello again");
        assertThat(createdOutput).isEqualTo("Created file: created.txt" + LINE_SEPARATOR + "Characters: 5");
        assertThat(overwrittenOutput).isEqualTo("Overwrote file: created.txt" + LINE_SEPARATOR + "Characters: 11");
        assertCompletedPart(recordedParts.subList(0, 1), CodegeistWriteFileTool.TOOL_NAME, createdOutput);
        assertCompletedPart(recordedParts.subList(1, 2), CodegeistWriteFileTool.TOOL_NAME, overwrittenOutput);
    }

    @Test
    void writeUsesConfiguredWorkspaceEncoding() throws IOException {
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistWriteFileTool.TOOL_NAME,
                """
                    {"path":"latin1.txt","content":"Gr\u00fc\u00dfe"}
                    """,
                recordedParts,
                "ISO-8859-1");

        assertThat(Files.readAllBytes(tempDir.resolve("latin1.txt")))
                .isEqualTo("Gr\u00fc\u00dfe".getBytes(StandardCharsets.ISO_8859_1));
        assertThat(output).isEqualTo("Created file: latin1.txt" + LINE_SEPARATOR + "Characters: 5");
        assertCompletedPart(recordedParts, CodegeistWriteFileTool.TOOL_NAME, output);
    }

    @Test
    void writeRejectsDirectoriesAndMissingParents() throws IOException {
        Files.createDirectory(tempDir.resolve("directory"));
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String directoryOutput = callTool(CodegeistWriteFileTool.TOOL_NAME,
                """
                    {"path":"directory","content":"hello"}
                    """,
                recordedParts);
        String missingParentOutput = callTool(CodegeistWriteFileTool.TOOL_NAME,
                """
                    {"path":"missing/child.txt","content":"hello"}
                    """,
                recordedParts);

        assertThat(directoryOutput).contains("Path is a directory");
        assertThat(missingParentOutput).contains("Parent directory does not exist");
        assertThat(recordedParts)
                .hasSize(2)
                .allSatisfy(part -> assertThat(part.getStatus()).isEqualTo(ToolSessionPartStatus.failed));
    }

    @Test
    void toolOutputsArePreviewBoundedAndPersistTheSamePreview() throws IOException {
        Files.writeString(tempDir.resolve("large.txt"), "x".repeat(ToolOutputBounds.MAX_PREVIEW_CHARS + 100));
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistReadFileTool.TOOL_NAME,
                """
                    {"path":"large.txt","limit":1}
                    """,
                recordedParts);

        assertThat(output).hasSizeLessThanOrEqualTo(ToolOutputBounds.MAX_PREVIEW_CHARS);
        assertThat(recordedParts).singleElement().satisfies(part -> assertThat(part.getOutputPreview()).isEqualTo(output));
    }

    private String callTool(String toolName, String input, List<ToolSessionPart> recordedParts) {
        return callTool(toolName, input, recordedParts, null);
    }

    private String callTool(String toolName, String input, List<ToolSessionPart> recordedParts, String encoding) {
        return localTools(encoding).callbacks(recordedParts::add).stream()
                .filter(callback -> callback.getToolDefinition().name().equals(toolName))
                .findFirst()
                .orElseThrow()
                .call(input);
    }

    private CodegeistLocalTools localTools() {
        return localTools(null);
    }

    private CodegeistLocalTools localTools(String encoding) {
        CodegeistConfig config = configWithWorkspace(tempDir, encoding);
        WorkspaceResolver resolver = new WorkspaceResolver(config);
        ReflectionTestUtils.setField(resolver, "workingDir", tempDir.toString());
        ToolOutputBounds bounds = new ToolOutputBounds();
        CodegeistFileToolSupport support = new CodegeistFileToolSupport(
                bounds,
                resolver,
                new CodegeistToolJsonMapper(),
                new CodegeistFileEncoding(config));
        return new CodegeistLocalTools(
                bounds,
                List.of(
                        new CodegeistReadFileTool(support),
                        new CodegeistListFileTool(support),
                        new CodegeistGlobFileTool(support),
                        new CodegeistGrepFileTool(support),
                        new CodegeistWriteFileTool(support)));
    }

    private CodegeistConfig configWithWorkspace(Path workspace) {
        return configWithWorkspace(workspace, null);
    }

    private CodegeistConfig configWithWorkspace(Path workspace, String encoding) {
        CodegeistConfig config = new CodegeistConfig();
        WorkspaceConfig workspaceConfig = new WorkspaceConfig();
        workspaceConfig.setDirectory(workspace.toString());
        if (encoding != null) {
            workspaceConfig.setEncoding(Charset.forName(encoding));
        }
        addRootElement(config, new WorkspaceRootElement(workspaceConfig));
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

    private void assertCompletedPart(List<ToolSessionPart> recordedParts, String toolName, String output) {
        assertThat(recordedParts).singleElement().satisfies(part -> {
            assertThat(part.getId()).isNotNull();
            assertThat(part.getTool()).isEqualTo(toolName);
            assertThat(part.getStatus()).isEqualTo(ToolSessionPartStatus.completed);
            assertThat(part.getOutputPreview()).isEqualTo(output);
        });
    }
}
