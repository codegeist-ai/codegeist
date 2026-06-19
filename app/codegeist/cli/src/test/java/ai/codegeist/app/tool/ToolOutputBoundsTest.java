package ai.codegeist.app.tool;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ToolOutputBoundsTest {

    private final ToolOutputBounds bounds = new ToolOutputBounds();

    @Test
    void previewNullReturnsEmptyString() {
        assertThat(bounds.preview(null)).isEmpty();
    }

    @Test
    void previewBelowLimitIsUnchanged() {
        assertThat(bounds.preview("hello")).isEqualTo("hello");
    }

    @Test
    void previewAtLimitIsUnchanged() {
        String text = "x".repeat(ToolOutputBounds.MAX_PREVIEW_CHARS);

        assertThat(bounds.preview(text)).isEqualTo(text);
    }

    @Test
    void previewAboveLimitIsCapped() {
        String text = "x".repeat(ToolOutputBounds.MAX_PREVIEW_CHARS + 1);

        assertThat(bounds.preview(text))
                .hasSize(ToolOutputBounds.MAX_PREVIEW_CHARS)
                .isEqualTo("x".repeat(ToolOutputBounds.MAX_PREVIEW_CHARS));
    }

    @Test
    void linePreviewNullReturnsEmptyString() {
        assertThat(bounds.linePreview(null)).isEmpty();
    }

    @Test
    void linePreviewAtLimitIsUnchanged() {
        String line = "x".repeat(ToolOutputBounds.MAX_LINE_CHARS);

        assertThat(bounds.linePreview(line)).isEqualTo(line);
    }

    @Test
    void linePreviewAboveLimitIsCapped() {
        String line = "x".repeat(ToolOutputBounds.MAX_LINE_CHARS + 1);

        assertThat(bounds.linePreview(line))
                .hasSize(ToolOutputBounds.MAX_LINE_CHARS)
                .isEqualTo("x".repeat(ToolOutputBounds.MAX_LINE_CHARS));
    }

    @Test
    void resultLimitDefaultsForNullZeroAndNegativeInput() {
        assertThat(bounds.cappedResultLimit(null)).isEqualTo(ToolOutputBounds.MAX_RESULTS);
        assertThat(bounds.cappedResultLimit(0)).isEqualTo(ToolOutputBounds.MAX_RESULTS);
        assertThat(bounds.cappedResultLimit(-1)).isEqualTo(ToolOutputBounds.MAX_RESULTS);
    }

    @Test
    void resultLimitCapsAboveMaximumAndPreservesRangeValues() {
        assertThat(bounds.cappedResultLimit(1)).isEqualTo(1);
        assertThat(bounds.cappedResultLimit(ToolOutputBounds.MAX_RESULTS)).isEqualTo(ToolOutputBounds.MAX_RESULTS);
        assertThat(bounds.cappedResultLimit(ToolOutputBounds.MAX_RESULTS + 1)).isEqualTo(ToolOutputBounds.MAX_RESULTS);
    }

    @Test
    void readLimitDefaultsForNullZeroAndNegativeInput() {
        assertThat(bounds.cappedReadLimit(null)).isEqualTo(ToolOutputBounds.DEFAULT_READ_LINES);
        assertThat(bounds.cappedReadLimit(0)).isEqualTo(ToolOutputBounds.DEFAULT_READ_LINES);
        assertThat(bounds.cappedReadLimit(-1)).isEqualTo(ToolOutputBounds.DEFAULT_READ_LINES);
    }

    @Test
    void readLimitCapsAboveDefaultAndPreservesRangeValues() {
        assertThat(bounds.cappedReadLimit(1)).isEqualTo(1);
        assertThat(bounds.cappedReadLimit(ToolOutputBounds.DEFAULT_READ_LINES))
                .isEqualTo(ToolOutputBounds.DEFAULT_READ_LINES);
        assertThat(bounds.cappedReadLimit(ToolOutputBounds.DEFAULT_READ_LINES + 1))
                .isEqualTo(ToolOutputBounds.DEFAULT_READ_LINES);
    }

    @Test
    void errorPreviewNullAndBlankReturnFallback() {
        assertThat(bounds.errorPreview(null)).isEqualTo("Tool failed");
        assertThat(bounds.errorPreview("   ")).isEqualTo("Tool failed");
    }

    @Test
    void errorPreviewCollapsesMultilineWhitespace() {
        assertThat(bounds.errorPreview("first\n\tsecond   third")).isEqualTo("first second third");
    }

    @Test
    void errorPreviewCapsLongMessages() {
        String message = "x".repeat(ToolOutputBounds.MAX_LINE_CHARS + 1);

        assertThat(bounds.errorPreview(message))
                .hasSize(ToolOutputBounds.MAX_LINE_CHARS)
                .isEqualTo("x".repeat(ToolOutputBounds.MAX_LINE_CHARS));
    }
}
