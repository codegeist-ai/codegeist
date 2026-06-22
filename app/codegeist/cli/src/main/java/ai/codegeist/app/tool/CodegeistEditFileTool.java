package ai.codegeist.app.tool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Exact text edit tool for one existing workspace-contained text file.
 *
 * <p>The tool intentionally combines OpenCode-style exact replacement safety with
 * Pi-style multi-edit input: each edit is matched against the original normalized
 * file content, every match must be unique, all ranges are validated before the
 * file is written once, and the persisted tool part receives only a bounded text
 * summary. This is not a sandbox or permission system; it is a pre-side-effect
 * containment check for this mutation tool.
 */
@Component
@RequiredArgsConstructor
final class CodegeistEditFileTool implements CodegeistLocalTool {

    static final String TOOL_NAME = "codegeist_edit";

    private static final String EDITS_FIELD = "edits";
    private static final String OLD_TEXT_FIELD = "oldText";
    private static final String NEW_TEXT_FIELD = "newText";
    private static final String REQUIRED_EDITS_MESSAGE = "Required field is missing: edits";
    private static final String EMPTY_EDITS_MESSAGE = "At least one edit is required";
    private static final String OLD_TEXT_EMPTY_MESSAGE_SUFFIX = ".oldText must not be empty. "
            + "Use codegeist_write to create or overwrite a file.";
    private static final String OLD_NEW_IDENTICAL_MESSAGE = "edits[%d].oldText and edits[%d].newText must differ";
    private static final String LF = "\n";
    private static final String CR = "\r";
    private static final String CRLF = "\r\n";
    private static final String UTF_BOM = "\uFEFF";
    private static final String OPERATION = "edit";

    private final CodegeistFileToolSupport support;

    private final CodegeistWorkingDirectoryGuard workingDirectoryGuard;

    private final CodegeistEditToolSettings settings;

    @Override
    public ToolDefinition definition() {
        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description("Replace one or more exact text blocks in an existing workspace file")
                .inputSchema(support.schema("""
                    "path":{"type":"string","description":"Existing file path under the active workspace"},
                    "edits":{"type":"array","description":"Exact replacements matched against the original file content","items":{"type":"object","properties":{"oldText":{"type":"string","description":"Exact text that must appear exactly once"},"newText":{"type":"string","description":"Replacement text"}},"required":["oldText","newText"],"additionalProperties":false}}
                    """, CodegeistFileToolSupport.PATH_FIELD, EDITS_FIELD))
                .build();
    }

    @Override
    public CodegeistToolResult execute(CodegeistToolInput toolInput) {
        Path workspace = support.currentWorkspace();
        EditToolInput input = support.parseInput(toolInput, EditToolInput.class);
        Path file = resolveContainedExistingFile(
                workspace,
                support.requireText(input.path(), CodegeistFileToolSupport.REQUIRED_PATH_MESSAGE));
        support.rejectBinaryFile(file, workspace);

        EditSource source = readSource(workspace, file);
        EditPlan plan = planEdit(workspace, file, input, source);
        String updatedNormalizedText = applyEdits(source.normalizedText(), plan.edits());
        if (source.normalizedText().equals(updatedNormalizedText)) {
            throw new CodegeistToolException(
                    "No changes made to " + support.displayPath(workspace, file)
                            + ". The replacements produced identical content.");
        }
        String updatedText = restoreSourceCharacteristics(source, updatedNormalizedText);
        writeIfUnchanged(workspace, file, source, updatedText);

        return new CodegeistToolResult(summary(workspace, file, plan));
    }

    /**
     * Resolves the model-provided file path against the active workspace and rejects
     * targets outside the allowed edit area before any file mutation can happen.
     *
     * <p>Relative paths are resolved against the normalized active workspace. Absolute
     * paths are accepted only when {@link CodegeistWorkingDirectoryGuard} allows the
     * final regular file target for the current config.
     */
    Path resolveContainedExistingFile(Path workspace, String pathText) {
        Path normalizedWorkspace = workspace.toAbsolutePath().normalize();
        Path candidate = support.resolvePath(normalizedWorkspace, pathText);
        return workingDirectoryGuard.requireExistingRegularFile(
                normalizedWorkspace,
                candidate,
                support.displayPath(normalizedWorkspace, candidate));
    }

    /**
     * Reads the original file bytes once and prepares the text for exact matching.
     *
     * <p>The returned source keeps the byte snapshot for the later stale-write check,
     * remembers whether a leading BOM was present, normalizes all matching text to LF,
     * and records whether CRLF should be restored after edits are applied.
     */
    EditSource readSource(Path workspace, Path file) {
        try {
            byte[] originalBytes = Files.readAllBytes(file);
            String decoded = decodeText(originalBytes, workspace, file);
            boolean bom = decoded.startsWith(UTF_BOM);
            String text = bom ? decoded.substring(UTF_BOM.length()) : decoded;
            return new EditSource(originalBytes, bom, normalizeToLf(text), detectLineEnding(text));
        }
        catch (CharacterCodingException exception) {
            throw new CodegeistToolException(
                    "File is not text in " + support.charset().displayName() + ": " + support.displayPath(workspace, file),
                    exception);
        }
        catch (IOException exception) {
            throw new CodegeistToolException("Failed to read file: " + support.displayPath(workspace, file), exception);
        }
    }

    /**
     * Validates the full multi-edit request before any write happens.
     *
     * <p>Every entry is matched against the same original normalized content, then the
     * validated ranges are sorted and checked for overlap. The plan also precomputes
     * summary-only data so execution can write once after all failure paths are known.
     */
    EditPlan planEdit(Path workspace, Path file, EditToolInput input, EditSource source) {
        if (input.edits() == null) {
            throw new CodegeistToolException(REQUIRED_EDITS_MESSAGE);
        }
        if (CollectionUtils.isEmpty(input.edits())) {
            throw new CodegeistToolException(EMPTY_EDITS_MESSAGE);
        }

        List<ValidatedEdit> edits = new ArrayList<>();
        for (int index = 0; index < input.edits().size(); index++) {
            edits.add(validateEdit(workspace, file, source.normalizedText(), input.edits().get(index), index));
        }
        edits.sort(Comparator.comparingInt(ValidatedEdit::start));
        for (int index = 1; index < edits.size(); index++) {
            ValidatedEdit previous = edits.get(index - 1);
            ValidatedEdit current = edits.get(index);
            if (previous.end() > current.start()) {
                throw new CodegeistToolException("edits[%d] and edits[%d] overlap in %s. "
                        .formatted(previous.inputIndex(), current.inputIndex(), support.displayPath(workspace, file))
                        + "Merge them into one edit or target disjoint regions.");
            }
        }

        String updatedText = applyEdits(source.normalizedText(), edits);
        DiffPreview diffPreview = diffPreview(edits);
        return new EditPlan(edits, firstChangedLine(source.normalizedText(), updatedText), diffPreview);
    }

    /**
     * Applies already validated, sorted, non-overlapping ranges to normalized text.
     *
     * <p>This method deliberately has no matching or overlap checks. Callers must pass
     * the output from {@link #planEdit(Path, Path, EditToolInput, EditSource)} so the
     * same original content coordinates are used for all replacements.
     */
    String applyEdits(String normalizedOriginalText, List<ValidatedEdit> edits) {
        StringBuilder result = new StringBuilder(normalizedOriginalText.length());
        int offset = 0;
        for (ValidatedEdit edit : edits) {
            result.append(normalizedOriginalText, offset, edit.start());
            result.append(edit.newText());
            offset = edit.end();
        }
        result.append(normalizedOriginalText.substring(offset));
        return result.toString();
    }

    /**
     * Writes the edited text only if the file still matches the original byte snapshot.
     *
     * <p>The stale-byte check protects against overwriting concurrent changes between
     * the initial read and the final write. Encoding failures are reported as handled
     * tool failures so the model receives a bounded error preview.
     */
    void writeIfUnchanged(Path workspace, Path file, EditSource source, String updatedText) {
        try {
            byte[] currentBytes = Files.readAllBytes(file);
            if (!Arrays.equals(currentBytes, source.originalBytes())) {
                throw new CodegeistToolException(
                        "File changed while editing: " + support.displayPath(workspace, file)
                                + ". Read it again before editing.");
            }
            Files.write(file,
                    encodeText(updatedText, workspace, file),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (CodegeistToolException exception) {
            throw exception;
        }
        catch (CharacterCodingException exception) {
            throw new CodegeistToolException(
                    "File is not text in " + support.charset().displayName() + ": " + support.displayPath(workspace, file),
                    exception);
        }
        catch (IOException exception) {
            throw new CodegeistToolException("Failed to edit file: " + support.displayPath(workspace, file), exception);
        }
    }

    /**
     * Validates one requested replacement and returns its original-content range.
     *
     * <p>Both old and new text are normalized to LF before comparison. The old text
     * must be non-empty, different from the replacement, and appear exactly once so a
     * model cannot accidentally mutate an ambiguous repeated fragment.
     */
    private ValidatedEdit validateEdit(Path workspace, Path file, String normalizedText, EditEntryInput input, int index) {
        String editPrefix = "edits[" + index + "]";
        if (input == null) {
            throw new CodegeistToolException(editPrefix + " is required");
        }
        if (input.oldText() == null) {
            throw new CodegeistToolException("Required text field is missing: " + editPrefix + "." + OLD_TEXT_FIELD);
        }
        if (input.newText() == null) {
            throw new CodegeistToolException("Required field is missing: " + editPrefix + "." + NEW_TEXT_FIELD);
        }

        String oldText = normalizeToLf(input.oldText());
        String newText = normalizeToLf(input.newText());
        if (oldText.isEmpty()) {
            throw new CodegeistToolException(editPrefix + OLD_TEXT_EMPTY_MESSAGE_SUFFIX);
        }
        if (oldText.equals(newText)) {
            throw new CodegeistToolException(OLD_NEW_IDENTICAL_MESSAGE.formatted(index, index));
        }

        int start = normalizedText.indexOf(oldText);
        if (start < 0) {
            throw new CodegeistToolException("Could not find " + editPrefix + " in " + support.displayPath(workspace, file)
                    + ". The oldText must match exactly, including whitespace and indentation.");
        }
        if (normalizedText.indexOf(oldText, start + oldText.length()) >= 0) {
            throw new CodegeistToolException("Found multiple exact matches for " + editPrefix + " in "
                    + support.displayPath(workspace, file) + ". Provide more surrounding context to make oldText unique.");
        }
        return new ValidatedEdit(index, oldText, newText, start, start + oldText.length());
    }

    private String restoreSourceCharacteristics(EditSource source, String updatedNormalizedText) {
        String restored = restoreLineEndings(updatedNormalizedText, source.lineEnding());
        return source.bom() ? UTF_BOM + restored : restored;
    }

    private String decodeText(byte[] bytes, Path workspace, Path file) throws CharacterCodingException {
        return support.charset().newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
    }

    private byte[] encodeText(String text, Path workspace, Path file) throws CharacterCodingException {
        ByteBuffer byteBuffer = support.charset().newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .encode(CharBuffer.wrap(text));
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return bytes;
    }

    private String summary(Path workspace, Path file, EditPlan plan) {
        return support.outputBounds().preview(("File: %s" + CodegeistFileToolSupport.LINE_SEPARATOR
                + "Operation: %s" + CodegeistFileToolSupport.LINE_SEPARATOR
                + "Replacements: %d" + CodegeistFileToolSupport.LINE_SEPARATOR
                + "First changed line: %s" + CodegeistFileToolSupport.LINE_SEPARATOR
                + "Diff truncated: %s" + CodegeistFileToolSupport.LINE_SEPARATOR
                + "Diff:" + CodegeistFileToolSupport.LINE_SEPARATOR
                + "```diff" + CodegeistFileToolSupport.LINE_SEPARATOR
                + "%s" + CodegeistFileToolSupport.LINE_SEPARATOR
                + "```")
                .formatted(
                        support.displayPath(workspace, file),
                        OPERATION,
                        plan.edits().size(),
                        plan.firstChangedLine() == 0 ? "n/a" : Integer.toString(plan.firstChangedLine()),
                        plan.diffPreview().truncated(),
                        plan.diffPreview().text()));
    }

    /**
     * Builds the compact diff block embedded in the tool result summary.
     *
     * <p>The preview is intentionally not a full unified diff. It shows each accepted
     * edit independently, applies configured line and character caps, and records
     * whether the raw preview was truncated before the final output cap is applied.
     */
    private DiffPreview diffPreview(List<ValidatedEdit> edits) {
        List<String> lines = new ArrayList<>();
        int lineLimit = settings.diffPreviewLines();
        for (ValidatedEdit edit : edits) {
            lines.add("@@ edit " + (edit.inputIndex() + 1) + " @@");
            lines.addAll(previewLines(edit.oldText(), "-", lineLimit));
            lines.addAll(previewLines(edit.newText(), "+", lineLimit));
        }

        String rawPreview = String.join(CodegeistFileToolSupport.LINE_SEPARATOR, lines);
        int characterLimit = settings.diffPreviewChars();
        if (rawPreview.length() <= characterLimit) {
            return new DiffPreview(rawPreview, false);
        }
        return new DiffPreview(rawPreview.substring(0, characterLimit), true);
    }

    /**
     * Renders one old or new edit body as prefixed, per-line bounded preview lines.
     */
    private List<String> previewLines(String value, String prefix, int lineLimit) {
        String[] lines = normalizeToLf(value).split(LF, -1);
        int shownLines = Math.min(lines.length, lineLimit);
        List<String> preview = new ArrayList<>();
        for (int index = 0; index < shownLines; index++) {
            preview.add(prefix + support.outputBounds().linePreview(lines[index]));
        }
        if (lines.length > shownLines) {
            preview.add(prefix + "...");
        }
        return preview;
    }

    /**
     * Finds the first changed one-based line for the human-readable summary.
     *
     * <p>The inputs are already LF-normalized, so counting newline characters before
     * the first differing offset is enough and avoids building a full line diff.
     */
    private int firstChangedLine(String originalText, String updatedText) {
        if (originalText.equals(updatedText)) {
            return 0;
        }
        int firstDifference = 0;
        int maximum = Math.min(originalText.length(), updatedText.length());
        while (firstDifference < maximum && originalText.charAt(firstDifference) == updatedText.charAt(firstDifference)) {
            firstDifference++;
        }
        int line = 1;
        for (int index = 0; index < firstDifference; index++) {
            if (originalText.charAt(index) == '\n') {
                line++;
            }
        }
        return line;
    }

    private static String detectLineEnding(String text) {
        return text.contains(CRLF) ? CRLF : LF;
    }

    private static String normalizeToLf(String text) {
        return text.replace(CRLF, LF).replace(CR, LF);
    }

    private static String restoreLineEndings(String text, String lineEnding) {
        return CRLF.equals(lineEnding) ? text.replace(LF, CRLF) : text;
    }

    record EditSource(byte[] originalBytes, boolean bom, String normalizedText, String lineEnding) {
    }

    private record EditToolInput(String path, List<EditEntryInput> edits) {
    }

    private record EditEntryInput(String oldText, String newText) {
    }

    private record EditPlan(List<ValidatedEdit> edits, int firstChangedLine, DiffPreview diffPreview) {
    }

    private record ValidatedEdit(int inputIndex, String oldText, String newText, int start, int end) {
    }

    private record DiffPreview(String text, boolean truncated) {
    }
}
