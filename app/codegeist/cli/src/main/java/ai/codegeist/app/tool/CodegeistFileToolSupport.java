package ai.codegeist.app.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Shared workspace, filesystem, parsing, and schema helpers for local file tools. */
@Component
@RequiredArgsConstructor
final class CodegeistFileToolSupport {

    static final String DEFAULT_PATH = ".";
    static final String PATH_FIELD = "path";
    static final String PATTERN_FIELD = "pattern";
    static final String CONTENT_FIELD = "content";
    static final String REQUIRED_PATH_MESSAGE = "Required text field is missing: path";
    static final String REQUIRED_PATTERN_MESSAGE = "Required text field is missing: pattern";
    static final String REQUIRED_CONTENT_MESSAGE = "Required field is missing: content";
    static final String LINE_SEPARATOR = System.lineSeparator();
    static final LinkOption[] NO_FOLLOW_LINKS = {LinkOption.NOFOLLOW_LINKS};
    private static final String JSON_TYPE_OBJECT = "object";

    private final ToolOutputBounds outputBounds;

    private final WorkspaceResolver workspaceResolver;

    private final CodegeistToolJsonMapper jsonMapper;

    private final CodegeistFileEncoding fileEncoding;

    /** Shared bounds used by file tools before text reaches the model or session store. */
    ToolOutputBounds outputBounds() {
        return outputBounds;
    }

    /** Charset selected by global workspace config for local file tool text I/O. */
    Charset charset() {
        return fileEncoding.currentCharset();
    }

    /** Resolve the configured workspace for one tool execution; this is not an access-control boundary. */
    Path currentWorkspace() {
        return workspaceResolver.currentWorkspace().toAbsolutePath().normalize();
    }

    /** Resolve relative model-supplied paths against the active workspace while accepting absolute paths. */
    Path resolvePath(Path workspace, String pathText) {
        Path path = Path.of(pathText);
        if (path.isAbsolute()) {
            return path.toAbsolutePath().normalize();
        }
        return workspace.resolve(path).normalize();
    }

    /** Optional file-tool path inputs default to the workspace root. */
    String defaultPath(String path) {
        return StringUtils.hasText(path) ? path : DEFAULT_PATH;
    }

    /** Validate required text fields after JSON parsing so tools return handled failures. */
    String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new CodegeistToolException(message);
        }
        return value;
    }

    /** Require a path to exist without following symlinks, using display paths in user-visible errors. */
    void requireExists(Path path, Path workspace) {
        if (!Files.exists(path, NO_FOLLOW_LINKS)) {
            throw new CodegeistToolException("Path does not exist: " + displayPath(workspace, path));
        }
    }

    /** Require a path to be a regular file without following symlinks. */
    void requireRegularFile(Path path, Path workspace) {
        if (!Files.isRegularFile(path, NO_FOLLOW_LINKS)) {
            throw new CodegeistToolException("Path is not a file: " + displayPath(workspace, path));
        }
    }

    /** Fail explicit read targets early when the bounded sample indicates binary content. */
    void rejectBinaryFile(Path file, Path workspace) {
        if (isBinaryFile(file)) {
            throw new CodegeistToolException("File is not text: " + displayPath(workspace, file));
        }
    }

    /** Detect NUL bytes in a bounded sample; grep uses this to skip binary candidates. */
    boolean isBinaryFile(Path file) {
        try (InputStream inputStream = Files.newInputStream(file)) {
            byte[] sample = inputStream.readNBytes(ToolOutputBounds.MAX_PREVIEW_CHARS);
            for (byte value : sample) {
                if (value == 0) {
                    return true;
                }
            }
            return false;
        }
        catch (IOException exception) {
            throw new CodegeistToolException("Failed to sample file: " + file.getFileName(), exception);
        }
    }

    /** Guard against NUL bytes encountered after the initial binary sample while reading text. */
    void rejectNulLine(String line, Path file, Path workspace) {
        if (line.indexOf('\0') >= 0) {
            throw new CodegeistToolException("File is not text: " + displayPath(workspace, file));
        }
    }

    /** Create a configured-charset reader that reports malformed or unmappable input as an error. */
    BufferedReader textReader(Path file) throws IOException {
        return new BufferedReader(new InputStreamReader(
                Files.newInputStream(file),
                charset().newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)));
    }

    /** Compile model-supplied grep regex into a handled tool failure when syntax is invalid. */
    Pattern regex(String pattern, boolean caseInsensitive) {
        try {
            return Pattern.compile(requireText(pattern, REQUIRED_PATTERN_MESSAGE),
                    caseInsensitive ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0);
        }
        catch (PatternSyntaxException exception) {
            throw new CodegeistToolException("Invalid regex: " + exception.getDescription(), exception);
        }
    }

    /** Build a Java NIO glob matcher and convert invalid glob syntax into a handled failure. */
    PathMatcher pathMatcher(String pattern) {
        try {
            return FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        }
        catch (IllegalArgumentException exception) {
            throw new CodegeistToolException("Invalid glob: " + pattern, exception);
        }
    }

    /** Match Java NIO globs and let double-star prefix patterns match direct base-path files too. */
    boolean matchesGlob(String pattern, PathMatcher matcher, Path relativePath) {
        if (matcher.matches(relativePath)) {
            return true;
        }
        return pattern.startsWith("**/") && pathMatcher(pattern.substring(3)).matches(relativePath);
    }

    /** Render one direct list entry with stable file/directory markers. */
    String entryPreview(Path workspace, Path path) {
        String displayPath = displayPath(workspace, path);
        if (Files.isDirectory(path, NO_FOLLOW_LINKS)) {
            return "[DIR] " + displayPath + "/";
        }
        return "[FILE] " + displayPath;
    }

    /** Prefer workspace-relative paths in tool output, falling back to normalized absolute paths. */
    String displayPath(Path workspace, Path path) {
        Path normalizedWorkspace = workspace.toAbsolutePath().normalize();
        Path normalizedPath = path.toAbsolutePath().normalize();
        if (normalizedPath.startsWith(normalizedWorkspace)) {
            Path relativePath = normalizedWorkspace.relativize(normalizedPath);
            if (relativePath.toString().isEmpty()) {
                return DEFAULT_PATH;
            }
            return normalizeSeparators(relativePath);
        }
        return normalizeSeparators(normalizedPath);
    }

    /** Parse a typed tool input record from the raw JSON payload supplied by Spring AI. */
    <T> T parseInput(CodegeistToolInput toolInput, Class<T> inputType) {
        try {
            return jsonMapper.readValue(toolInput.json(), inputType);
        }
        catch (JsonProcessingException exception) {
            throw new CodegeistToolException("Invalid tool input JSON", exception);
        }
    }

    /** Assemble explicit object schemas for Spring AI tool definitions. */
    String schema(String properties, String... requiredFields) {
        String required = requiredFields.length == 0
                ? "[]"
                : "[\"" + String.join("\",\"", requiredFields) + "\"]";
        return """
            {"type":"%s","properties":{%s},"required":%s,"additionalProperties":false}
            """.formatted(JSON_TYPE_OBJECT, properties.strip(), required).strip();
    }

    private String normalizeSeparators(Path path) {
        return path.toString().replace(path.getFileSystem().getSeparator(), "/");
    }
}
