package ai.codegeist.app.tool;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Shared deterministic bounds for tool output before it reaches session storage or
 * chat-facing summaries.
 *
 * <p>This class deliberately returns plain strings and numeric limits only. The
 * current tool slice does not expose truncation metadata, preview objects, or
 * request-specific cap increases. Callers may request a lower result/read limit,
 * but cannot raise the built-in maximums.
 */
@Component
public class ToolOutputBounds {

    public static final int MAX_PREVIEW_CHARS = 8000;
    public static final int MAX_LINE_CHARS = 500;
    public static final int MAX_RESULTS = 200;
    public static final int DEFAULT_READ_LINES = 200;
    private static final String DEFAULT_ERROR_PREVIEW = "Tool failed";
    private static final String WHITESPACE_PATTERN = "\\s+";
    private static final String SINGLE_SPACE = " ";

    /** Returns a nullable text value capped to the session-safe preview length. */
    public String preview(String text) {
        return capped(text, MAX_PREVIEW_CHARS);
    }

    /** Returns one output line capped for compact status or error displays. */
    public String linePreview(String line) {
        return capped(line, MAX_LINE_CHARS);
    }

    /** Returns a positive requested result limit capped by the global maximum. */
    public int cappedResultLimit(Integer requestedLimit) {
        return cap(requestedLimit, MAX_RESULTS);
    }

    /** Returns a positive requested read-line limit capped by the default read cap. */
    public int cappedReadLimit(Integer requestedLimit) {
        return cap(requestedLimit, DEFAULT_READ_LINES);
    }

    /** Normalizes arbitrary failure text into a compact, single-line preview. */
    public String errorPreview(String message) {
        String normalized = StringUtils.hasText(message)
                ? message.replaceAll(WHITESPACE_PATTERN, SINGLE_SPACE).trim()
                : DEFAULT_ERROR_PREVIEW;
        return capped(normalized, MAX_LINE_CHARS);
    }

    private String capped(String value, int maxCharacters) {
        String normalized = value == null ? "" : value;
        if (normalized.length() <= maxCharacters) {
            return normalized;
        }
        return normalized.substring(0, maxCharacters);
    }

    private int cap(Integer requestedLimit, int maximum) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return maximum;
        }
        return Math.min(requestedLimit, maximum);
    }
}
