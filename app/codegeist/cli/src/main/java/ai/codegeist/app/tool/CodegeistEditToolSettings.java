package ai.codegeist.app.tool;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistEditToolConfig;
import ai.codegeist.app.config.ToolsConfig;
import ai.codegeist.app.config.ToolsRootElement;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves bounded runtime settings for {@code codegeist_edit}.
 *
 * <p>Direct {@code codegeist.yml} values live under
 * {@code tools.codegeist-edit}. This component keeps the file-edit tool focused on
 * mutation logic while centralizing defaulting and caps for output-preview tuning.
 */
@Component
@RequiredArgsConstructor
final class CodegeistEditToolSettings {

    static final int DEFAULT_DIFF_PREVIEW_LINES = 6;
    static final int DEFAULT_DIFF_PREVIEW_CHARS = ToolOutputBounds.MAX_PREVIEW_CHARS / 2;

    private final CodegeistConfig config;

    int diffPreviewLines() {
        return configuredEdit()
                .map(CodegeistEditToolConfig::getDiffPreviewLines)
                .map(value -> boundedPositive(value, DEFAULT_DIFF_PREVIEW_LINES, ToolOutputBounds.MAX_RESULTS))
                .orElse(DEFAULT_DIFF_PREVIEW_LINES);
    }

    int diffPreviewChars() {
        return configuredEdit()
                .map(CodegeistEditToolConfig::getDiffPreviewChars)
                .map(value -> boundedPositive(value, DEFAULT_DIFF_PREVIEW_CHARS, ToolOutputBounds.MAX_PREVIEW_CHARS))
                .orElse(DEFAULT_DIFF_PREVIEW_CHARS);
    }

    private Optional<CodegeistEditToolConfig> configuredEdit() {
        return config.rootElement(ToolsRootElement.class)
                .map(ToolsRootElement::getConfig)
                .map(ToolsConfig::getCodegeistEdit);
    }

    private int boundedPositive(Integer configuredValue, int defaultValue, int maximumValue) {
        if (configuredValue == null || configuredValue <= 0) {
            return defaultValue;
        }
        return Math.min(configuredValue, maximumValue);
    }
}
