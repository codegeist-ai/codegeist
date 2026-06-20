package ai.codegeist.app.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
final class CodegeistGrepFileTool implements CodegeistLocalTool {

    static final String TOOL_NAME = "codegeist_grep";

    private final CodegeistFileToolSupport support;

    @Override
    public ToolDefinition definition() {
        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description("Search text files with a Java regular expression using the configured workspace encoding")
                .inputSchema(support.schema("""
                    "pattern":{"type":"string","description":"Java regular expression"},
                    "path":{"type":"string","description":"File or directory path"},
                    "include":{"type":"string","description":"Optional Java glob include filter"},
                    "caseInsensitive":{"type":"boolean","description":"Use case-insensitive matching"},
                    "limit":{"type":"integer","description":"Maximum matching lines to return"}
                    """, CodegeistFileToolSupport.PATTERN_FIELD))
                .build();
    }

    @Override
    public CodegeistToolResult execute(CodegeistToolInput toolInput) {
        Path workspace = support.currentWorkspace();
        GrepToolInput input = support.parseInput(toolInput, GrepToolInput.class);
        Pattern regex = support.regex(input.pattern(), Boolean.TRUE.equals(input.caseInsensitive()));
        Path target = support.resolvePath(workspace, support.defaultPath(input.path()));
        support.requireExists(target, workspace);
        int limit = support.outputBounds().cappedResultLimit(input.limit());
        List<Path> candidateFiles = grepCandidates(workspace, target, input.include());

        List<String> matches = new ArrayList<>();
        for (Path candidateFile : candidateFiles) {
            if (matches.size() >= limit) {
                break;
            }
            if (support.isBinaryFile(candidateFile)) {
                continue;
            }
            addMatches(workspace, candidateFile, regex, limit, matches);
        }
        return new CodegeistToolResult(
                support.outputBounds().preview(String.join(CodegeistFileToolSupport.LINE_SEPARATOR, matches)));
    }

    private List<Path> grepCandidates(Path workspace, Path target, String include) {
        if (Files.isRegularFile(target, CodegeistFileToolSupport.NO_FOLLOW_LINKS)) {
            return includeMatches(include, target.getParent(), target) ? List.of(target) : List.of();
        }
        if (!Files.isDirectory(target, CodegeistFileToolSupport.NO_FOLLOW_LINKS)) {
            throw new CodegeistToolException(
                    "Path is not a file or directory: " + support.displayPath(workspace, target));
        }

        try (Stream<Path> paths = Files.walk(target)) {
            return paths
                    .filter(path -> Files.isRegularFile(path, CodegeistFileToolSupport.NO_FOLLOW_LINKS))
                    .filter(path -> includeMatches(include, target, path))
                    .sorted(Comparator.comparing(path -> support.displayPath(workspace, path)))
                    .toList();
        }
        catch (IOException exception) {
            throw new CodegeistToolException(
                    "Failed to enumerate files under: " + support.displayPath(workspace, target), exception);
        }
    }

    private boolean includeMatches(String include, Path baseDirectory, Path candidate) {
        if (!StringUtils.hasText(include)) {
            return true;
        }
        PathMatcher matcher = support.pathMatcher(include);
        Path relative = baseDirectory.relativize(candidate);
        return support.matchesGlob(include, matcher, relative);
    }

    private void addMatches(Path workspace, Path candidateFile, Pattern regex, int limit, List<String> matches) {
        try (BufferedReader reader = support.textReader(candidateFile)) {
            String line;
            int lineNumber = 1;
            while (matches.size() < limit && (line = reader.readLine()) != null) {
                if (line.indexOf('\0') >= 0) {
                    return;
                }
                if (regex.matcher(line).find()) {
                    matches.add("%s:%d: %s".formatted(
                            support.displayPath(workspace, candidateFile),
                            lineNumber,
                            support.outputBounds().linePreview(line)));
                }
                lineNumber++;
            }
        }
        catch (CharacterCodingException exception) {
            log.debug("Skipping grep candidate with malformed text for {} encoding: {}",
                    support.charset().displayName(),
                    support.displayPath(workspace, candidateFile),
                    exception);
        }
        catch (IOException exception) {
            throw new CodegeistToolException(
                    "Failed to grep file: " + support.displayPath(workspace, candidateFile), exception);
        }
    }

    private record GrepToolInput(
            String pattern,
            String path,
            String include,
            Boolean caseInsensitive,
            Integer limit) {
    }
}
