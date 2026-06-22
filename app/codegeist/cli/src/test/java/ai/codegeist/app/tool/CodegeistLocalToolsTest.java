package ai.codegeist.app.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistConfigElement;
import ai.codegeist.app.config.CodegeistConfigRootElement;
import ai.codegeist.app.config.CodegeistEditToolConfig;
import ai.codegeist.app.config.ToolsConfig;
import ai.codegeist.app.config.ToolsRootElement;
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
import java.util.stream.IntStream;
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
                        CodegeistWriteFileTool.TOOL_NAME,
                        CodegeistEditFileTool.TOOL_NAME);
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
    void editSchemaExposesOnlyPathAndEdits() {
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String schema = localTools().callbacks(recordedParts::add).stream()
                .filter(callback -> callback.getToolDefinition().name().equals(CodegeistEditFileTool.TOOL_NAME))
                .findFirst()
                .orElseThrow()
                .getToolDefinition()
                .inputSchema();

        assertThat(schema)
                .contains("\"path\"", "\"edits\"", "\"oldText\"", "\"newText\"")
                .doesNotContain("oldString", "newString", "replaceAll", "patchText");
    }

    @Test
    void editAppliesSingleExactReplacementAndRecordsToolPart() throws IOException {
        Files.writeString(tempDir.resolve("notes.txt"), "alpha\nbeta\ngamma\n");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"notes.txt","edits":[{"oldText":"beta","newText":"delta"}]}
                    """,
                recordedParts);

        assertThat(Files.readString(tempDir.resolve("notes.txt"))).isEqualTo("alpha\ndelta\ngamma\n");
        assertThat(output).contains(
                "File: notes.txt",
                "Operation: edit",
                "Replacements: 1",
                "First changed line: 2",
                "Diff truncated: false",
                "```diff",
                "-beta",
                "+delta");
        assertCompletedPart(recordedParts, CodegeistEditFileTool.TOOL_NAME, output);
    }

    @Test
    void editAcceptsAbsolutePathInsideWorkspace() throws IOException {
        Path file = tempDir.resolve("absolute.txt");
        Files.writeString(file, "before");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"%s","edits":[{"oldText":"before","newText":"after"}]}
                    """.formatted(jsonPath(file)),
                recordedParts);

        assertThat(output).contains("File: absolute.txt", "Replacements: 1");
        assertThat(Files.readString(file)).isEqualTo("after");
        assertCompletedPart(recordedParts, CodegeistEditFileTool.TOOL_NAME, output);
    }

    @Test
    void editAppliesMultipleDisjointReplacementsAgainstOriginalContent() throws IOException {
        Files.writeString(tempDir.resolve("multi.txt"), "foo\nbar\nbaz\ngamma\n");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"multi.txt","edits":[
                      {"oldText":"foo\\n","newText":"foo bar\\n"},
                      {"oldText":"bar\\n","newText":"BAR\\n"},
                      {"oldText":"gamma\\n","newText":"GAMMA\\n"}
                    ]}
                    """,
                recordedParts);

        assertThat(Files.readString(tempDir.resolve("multi.txt"))).isEqualTo("foo bar\nBAR\nbaz\nGAMMA\n");
        assertThat(output).contains("Replacements: 3", "foo bar", "GAMMA");
        assertCompletedPart(recordedParts, CodegeistEditFileTool.TOOL_NAME, output);
    }

    @Test
    void editRejectsNoMatchAndAmbiguousMatchWithoutMutation() throws IOException {
        Path file = tempDir.resolve("matches.txt");
        Files.writeString(file, "same same\n");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String noMatchOutput = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"matches.txt","edits":[{"oldText":"missing","newText":"after"}]}
                    """,
                recordedParts);
        String ambiguousOutput = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"matches.txt","edits":[{"oldText":"same","newText":"after"}]}
                    """,
                recordedParts);

        assertThat(noMatchOutput).contains("Could not find edits[0]");
        assertThat(ambiguousOutput).contains("Found multiple exact matches for edits[0]");
        assertThat(Files.readString(file)).isEqualTo("same same\n");
        assertFailedParts(recordedParts, 2);
    }

    @Test
    void editRejectsNoPartialMutationWhenOneEditFails() throws IOException {
        Path file = tempDir.resolve("no-partial.txt");
        Files.writeString(file, "alpha\nbeta\ngamma\n");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"no-partial.txt","edits":[
                      {"oldText":"alpha\\n","newText":"ALPHA\\n"},
                      {"oldText":"missing\\n","newText":"MISSING\\n"}
                    ]}
                    """,
                recordedParts);

        assertThat(output).contains("Could not find edits[1]");
        assertThat(Files.readString(file)).isEqualTo("alpha\nbeta\ngamma\n");
        assertFailedParts(recordedParts, 1);
    }

    @Test
    void editRejectsOutsideWorkspacePathBeforeMutation() throws IOException {
        Path workspace = Files.createDirectory(tempDir.resolve("workspace"));
        Path outside = Files.createDirectory(tempDir.resolve("outside"));
        Path outsideFile = outside.resolve("escape.txt");
        Files.writeString(outsideFile, "before");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"%s","edits":[{"oldText":"before","newText":"after"}]}
                    """.formatted(jsonPath(outsideFile)),
                recordedParts,
                workspace,
                null);

        assertThat(output).contains("Path escapes workspace");
        assertThat(Files.readString(outsideFile)).isEqualTo("before");
        assertFailedParts(recordedParts, 1);
    }

    @Test
    void editRejectsTraversalOutsideWorkspaceBeforeMutation() throws IOException {
        Path workspace = Files.createDirectory(tempDir.resolve("workspace"));
        Path outside = Files.createDirectory(tempDir.resolve("outside"));
        Path outsideFile = outside.resolve("escape.txt");
        Files.writeString(outsideFile, "before");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"../outside/escape.txt","edits":[{"oldText":"before","newText":"after"}]}
                    """,
                recordedParts,
                workspace,
                null);

        assertThat(output).contains("Path escapes workspace");
        assertThat(Files.readString(outsideFile)).isEqualTo("before");
        assertFailedParts(recordedParts, 1);
    }

    @Test
    void editRejectsSymlinkEscapeBeforeMutation() throws IOException {
        Path workspace = Files.createDirectory(tempDir.resolve("workspace"));
        Path outside = Files.createDirectory(tempDir.resolve("outside"));
        Path outsideFile = outside.resolve("target.txt");
        Path symlink = workspace.resolve("link.txt");
        Files.writeString(outsideFile, "before");
        Files.createSymbolicLink(symlink, outsideFile);
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"link.txt","edits":[{"oldText":"before","newText":"after"}]}
                    """,
                recordedParts,
                workspace,
                null);

        assertThat(output).contains("Path escapes workspace");
        assertThat(Files.readString(outsideFile)).isEqualTo("before");
        assertFailedParts(recordedParts, 1);
    }

    @Test
    void editAllowsOutsideWorkspacePathWhenDirGuardIsDisabled() throws IOException {
        Path workspace = Files.createDirectory(tempDir.resolve("workspace"));
        Path outside = Files.createDirectory(tempDir.resolve("outside"));
        Path outsideFile = outside.resolve("external.txt");
        Files.writeString(outsideFile, "before");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"%s","edits":[{"oldText":"before","newText":"after"}]}
                    """.formatted(jsonPath(outsideFile)),
                recordedParts,
                workspace,
                null,
                true);

        assertThat(output).contains("File: ", "Replacements: 1");
        assertThat(Files.readString(outsideFile)).isEqualTo("after");
        assertCompletedPart(recordedParts, CodegeistEditFileTool.TOOL_NAME, output);
    }

    @Test
    void editRejectsMissingFilesAndDirectories() throws IOException {
        Files.createDirectory(tempDir.resolve("directory"));
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String missingOutput = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"missing.txt","edits":[{"oldText":"a","newText":"b"}]}
                    """,
                recordedParts);
        String directoryOutput = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"directory","edits":[{"oldText":"a","newText":"b"}]}
                    """,
                recordedParts);

        assertThat(missingOutput).contains("Path does not exist");
        assertThat(directoryOutput).contains("Path is not a file");
        assertFailedParts(recordedParts, 2);
    }

    @Test
    void editRejectsInvalidEditInputs() throws IOException {
        Path file = tempDir.resolve("invalid-inputs.txt");
        Files.writeString(file, "hello\n");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        List<String> outputs = List.of(
                callTool(CodegeistEditFileTool.TOOL_NAME, """
                    {"path":"invalid-inputs.txt"}
                    """, recordedParts),
                callTool(CodegeistEditFileTool.TOOL_NAME, """
                    {"path":"invalid-inputs.txt","edits":[]}
                    """, recordedParts),
                callTool(CodegeistEditFileTool.TOOL_NAME, """
                    {"path":"invalid-inputs.txt","edits":[null]}
                    """, recordedParts),
                callTool(CodegeistEditFileTool.TOOL_NAME, """
                    {"path":"invalid-inputs.txt","edits":[{"oldText":"","newText":"x"}]}
                    """, recordedParts),
                callTool(CodegeistEditFileTool.TOOL_NAME, """
                    {"path":"invalid-inputs.txt","edits":[{"oldText":"hello","newText":null}]}
                    """, recordedParts),
                callTool(CodegeistEditFileTool.TOOL_NAME, """
                    {"path":"invalid-inputs.txt","edits":[{"oldText":"hello","newText":"hello"}]}
                    """, recordedParts));

        assertThat(outputs.get(0)).contains("Required field is missing: edits");
        assertThat(outputs.get(1)).contains("At least one edit is required");
        assertThat(outputs.get(2)).contains("edits[0] is required");
        assertThat(outputs.get(3)).contains("edits[0].oldText must not be empty");
        assertThat(outputs.get(4)).contains("Required field is missing: edits[0].newText");
        assertThat(outputs.get(5)).contains("edits[0].oldText and edits[0].newText must differ");
        assertThat(Files.readString(file)).isEqualTo("hello\n");
        assertFailedParts(recordedParts, outputs.size());
    }

    @Test
    void editRejectsOverlappingMultiEdits() throws IOException {
        Path file = tempDir.resolve("overlap.txt");
        Files.writeString(file, "one\ntwo\nthree\n");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"overlap.txt","edits":[
                      {"oldText":"one\\ntwo\\n","newText":"ONE\\nTWO\\n"},
                      {"oldText":"two\\nthree\\n","newText":"TWO\\nTHREE\\n"}
                    ]}
                    """,
                recordedParts);

        assertThat(output).contains("overlap");
        assertThat(Files.readString(file)).isEqualTo("one\ntwo\nthree\n");
        assertFailedParts(recordedParts, 1);
    }

    @Test
    void editAllowsEmptyNewTextDeletion() throws IOException {
        Path file = tempDir.resolve("delete.txt");
        Files.writeString(file, "keep remove keep");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"delete.txt","edits":[{"oldText":" remove","newText":""}]}
                    """,
                recordedParts);

        assertThat(Files.readString(file)).isEqualTo("keep keep");
        assertThat(output).contains("Replacements: 1", "- remove", "+");
        assertCompletedPart(recordedParts, CodegeistEditFileTool.TOOL_NAME, output);
    }

    @Test
    void editPreservesBomAndCrlfLineEndings() throws IOException {
        Path file = tempDir.resolve("windows.txt");
        Files.writeString(file, "\uFEFFbefore\r\nrest\r\n");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"windows.txt","edits":[{"oldText":"before\\nrest","newText":"after\\nrest"}]}
                    """,
                recordedParts);

        assertThat(Files.readString(file)).isEqualTo("\uFEFFafter\r\nrest\r\n");
        assertThat(output).contains("Replacements: 1", "-before", "+after");
        assertCompletedPart(recordedParts, CodegeistEditFileTool.TOOL_NAME, output);
    }

    @Test
    void editRejectsStaleContentBeforeWrite() throws IOException {
        Path file = tempDir.resolve("stale.txt");
        Files.writeString(file, "before");
        CodegeistEditFileTool editTool = editTool(tempDir, null);
        CodegeistEditFileTool.EditSource source = editTool.readSource(tempDir, file);

        Files.writeString(file, "current");

        assertThatExceptionOfType(CodegeistToolException.class)
                .isThrownBy(() -> editTool.writeIfUnchanged(tempDir, file, source, "after"))
                .withMessageContaining("File changed while editing");
        assertThat(Files.readString(file)).isEqualTo("current");
    }

    @Test
    void editOutputIsBoundedAndPersistedAsTheSamePreview() throws IOException {
        String oldText = "a".repeat(ToolOutputBounds.MAX_PREVIEW_CHARS + 100);
        String newText = "b".repeat(ToolOutputBounds.MAX_PREVIEW_CHARS + 100);
        Files.writeString(tempDir.resolve("large-edit.txt"), oldText);
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"large-edit.txt","edits":[{"oldText":"%s","newText":"%s"}]}
                    """.formatted(oldText, newText),
                recordedParts);

        assertThat(output).hasSizeLessThanOrEqualTo(ToolOutputBounds.MAX_PREVIEW_CHARS);
        assertThat(recordedParts).singleElement().satisfies(part -> assertThat(part.getOutputPreview()).isEqualTo(output));
    }

    @Test
    void editUsesConfiguredDiffPreviewLineLimit() throws IOException {
        Path file = tempDir.resolve("line-limit.txt");
        Files.writeString(file, "one\ntwo\nthree\nfour\n");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"line-limit.txt","edits":[{"oldText":"one\\ntwo\\nthree\\nfour\\n","newText":"ONE\\nTWO\\nTHREE\\nFOUR\\n"}]}
                    """,
                recordedParts,
                tempDir,
                null,
                false,
                2,
                null);

        assertThat(Files.readString(file)).isEqualTo("ONE\nTWO\nTHREE\nFOUR\n");
        assertThat(output)
                .contains("Diff truncated: false", "-one", "-two", "-...", "+ONE", "+TWO", "+...")
                .doesNotContain("-three", "+THREE");
        assertCompletedPart(recordedParts, CodegeistEditFileTool.TOOL_NAME, output);
    }

    @Test
    void editUsesConfiguredDiffPreviewCharacterLimit() throws IOException {
        Path file = tempDir.resolve("char-limit.txt");
        Files.writeString(file, "abcdefghij\n");
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"char-limit.txt","edits":[{"oldText":"abcdefghij\\n","newText":"ABCDEFGHIJ\\n"}]}
                    """,
                recordedParts,
                tempDir,
                null,
                false,
                null,
                12);

        assertThat(Files.readString(file)).isEqualTo("ABCDEFGHIJ\n");
        assertThat(output)
                .contains("Diff truncated: true", "@@ edit 1 @")
                .doesNotContain("-abcdefghij", "+ABCDEFGHIJ");
        assertCompletedPart(recordedParts, CodegeistEditFileTool.TOOL_NAME, output);
    }

    @Test
    void editLargeMultiEditDiffCollapsesUnchangedGaps() throws IOException {
        Path file = tempDir.resolve("large-gap.txt");
        String content = String.join("\n", IntStream.rangeClosed(1, 300)
                .mapToObj(index -> "line " + index)
                .toList()) + "\n";
        Files.writeString(file, content);
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String output = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"large-gap.txt","edits":[
                      {"oldText":"line 50\\n","newText":"LINE 50\\n"},
                      {"oldText":"line 150\\n","newText":"LINE 150\\n"},
                      {"oldText":"line 250\\n","newText":"LINE 250\\n"}
                    ]}
                    """,
                recordedParts);

        assertThat(output)
                .contains("LINE 50", "LINE 150", "LINE 250")
                .doesNotContain("line 100");
        assertCompletedPart(recordedParts, CodegeistEditFileTool.TOOL_NAME, output);
    }

    @Test
    void editRejectsBinaryOrMalformedTextFiles() throws IOException {
        Files.write(tempDir.resolve("binary.dat"), new byte[] {'a', 0, 'b'});
        Files.write(tempDir.resolve("malformed.txt"), new byte[] {(byte) 0xc3, 0x28});
        List<ToolSessionPart> recordedParts = new ArrayList<>();

        String binaryOutput = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"binary.dat","edits":[{"oldText":"a","newText":"b"}]}
                    """,
                recordedParts);
        String malformedOutput = callTool(CodegeistEditFileTool.TOOL_NAME,
                """
                    {"path":"malformed.txt","edits":[{"oldText":"a","newText":"b"}]}
                    """,
                recordedParts);

        assertThat(binaryOutput).contains("File is not text");
        assertThat(malformedOutput).contains("File is not text in UTF-8");
        assertFailedParts(recordedParts, 2);
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
        return callTool(toolName, input, recordedParts, tempDir, encoding);
    }

    private String callTool(
            String toolName,
            String input,
            List<ToolSessionPart> recordedParts,
            Path workspace,
            String encoding) {
        return callTool(toolName, input, recordedParts, workspace, encoding, false);
    }

    private String callTool(
            String toolName,
            String input,
            List<ToolSessionPart> recordedParts,
            Path workspace,
            String encoding,
            boolean dirGuardDisabled) {
        return callTool(toolName, input, recordedParts, workspace, encoding, dirGuardDisabled, null, null);
    }

    private String callTool(
            String toolName,
            String input,
            List<ToolSessionPart> recordedParts,
            Path workspace,
            String encoding,
            boolean dirGuardDisabled,
            Integer diffPreviewLines,
            Integer diffPreviewChars) {
        return localTools(workspace, encoding, dirGuardDisabled, diffPreviewLines, diffPreviewChars)
                .callbacks(recordedParts::add)
                .stream()
                .filter(callback -> callback.getToolDefinition().name().equals(toolName))
                .findFirst()
                .orElseThrow()
                .call(input);
    }

    private CodegeistLocalTools localTools() {
        return localTools(null);
    }

    private CodegeistLocalTools localTools(String encoding) {
        return localTools(tempDir, encoding);
    }

    private CodegeistLocalTools localTools(Path workspace, String encoding) {
        return localTools(workspace, encoding, false);
    }

    private CodegeistLocalTools localTools(Path workspace, String encoding, boolean dirGuardDisabled) {
        return localTools(workspace, encoding, dirGuardDisabled, null, null);
    }

    private CodegeistLocalTools localTools(
            Path workspace,
            String encoding,
            boolean dirGuardDisabled,
            Integer diffPreviewLines,
            Integer diffPreviewChars) {
        CodegeistConfig config = configWithWorkspace(
                workspace,
                encoding,
                dirGuardDisabled,
                diffPreviewLines,
                diffPreviewChars);
        WorkspaceResolver resolver = new WorkspaceResolver(config);
        ReflectionTestUtils.setField(resolver, "workingDir", workspace.toString());
        ToolOutputBounds bounds = new ToolOutputBounds();
        CodegeistFileToolSupport support = fileToolSupport(config, resolver, bounds);
        CodegeistWorkingDirectoryGuard workingDirectoryGuard = new CodegeistWorkingDirectoryGuard(config);
        CodegeistEditToolSettings editToolSettings = new CodegeistEditToolSettings(config);
        return new CodegeistLocalTools(
                bounds,
                List.of(
                        new CodegeistReadFileTool(support),
                        new CodegeistListFileTool(support),
                        new CodegeistGlobFileTool(support),
                        new CodegeistGrepFileTool(support),
                        new CodegeistWriteFileTool(support),
                        new CodegeistEditFileTool(support, workingDirectoryGuard, editToolSettings)));
    }

    private CodegeistEditFileTool editTool(Path workspace, String encoding) {
        CodegeistConfig config = configWithWorkspace(workspace, encoding);
        WorkspaceResolver resolver = new WorkspaceResolver(config);
        ReflectionTestUtils.setField(resolver, "workingDir", workspace.toString());
        return new CodegeistEditFileTool(
                fileToolSupport(config, resolver, new ToolOutputBounds()),
                new CodegeistWorkingDirectoryGuard(config),
                new CodegeistEditToolSettings(config));
    }

    private CodegeistFileToolSupport fileToolSupport(
            CodegeistConfig config,
            WorkspaceResolver resolver,
            ToolOutputBounds bounds) {
        return new CodegeistFileToolSupport(
                bounds,
                resolver,
                new CodegeistToolJsonMapper(),
                new CodegeistFileEncoding(config));
    }

    private CodegeistConfig configWithWorkspace(Path workspace) {
        return configWithWorkspace(workspace, null);
    }

    private CodegeistConfig configWithWorkspace(Path workspace, String encoding) {
        return configWithWorkspace(workspace, encoding, false);
    }

    private CodegeistConfig configWithWorkspace(Path workspace, String encoding, boolean dirGuardDisabled) {
        return configWithWorkspace(workspace, encoding, dirGuardDisabled, null, null);
    }

    private CodegeistConfig configWithWorkspace(
            Path workspace,
            String encoding,
            boolean dirGuardDisabled,
            Integer diffPreviewLines,
            Integer diffPreviewChars) {
        CodegeistConfig config = new CodegeistConfig();
        WorkspaceConfig workspaceConfig = new WorkspaceConfig();
        workspaceConfig.setDirectory(workspace.toString());
        workspaceConfig.setDirGuardDisabled(dirGuardDisabled);
        if (encoding != null) {
            workspaceConfig.setEncoding(Charset.forName(encoding));
        }
        addRootElement(config, new WorkspaceRootElement(workspaceConfig));
        if (diffPreviewLines != null || diffPreviewChars != null) {
            CodegeistEditToolConfig editToolConfig = new CodegeistEditToolConfig();
            editToolConfig.setDiffPreviewLines(diffPreviewLines);
            editToolConfig.setDiffPreviewChars(diffPreviewChars);
            ToolsConfig toolsConfig = new ToolsConfig();
            toolsConfig.setCodegeistEdit(editToolConfig);
            addRootElement(config, new ToolsRootElement(toolsConfig));
        }
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

    private void assertFailedParts(List<ToolSessionPart> recordedParts, int count) {
        assertThat(recordedParts)
                .hasSize(count)
                .allSatisfy(part -> {
                    assertThat(part.getTool()).isEqualTo(CodegeistEditFileTool.TOOL_NAME);
                    assertThat(part.getStatus()).isEqualTo(ToolSessionPartStatus.failed);
                    assertThat(part.getOutputPreview()).isNotBlank();
                });
    }

    private String jsonPath(Path path) {
        return path.toString().replace("\\", "\\\\");
    }
}
