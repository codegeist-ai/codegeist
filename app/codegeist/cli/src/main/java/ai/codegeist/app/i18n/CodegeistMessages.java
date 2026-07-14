package ai.codegeist.app.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * Resolves Codegeist user-visible message keys through Spring resource bundles.
 *
 * <p>English defaults live in {@code src/main/resources/messages.properties}. This
 * service keeps application code on message keys without adding a parallel Java
 * enum/default catalog.</p>
 */
@Component
@RequiredArgsConstructor
public class CodegeistMessages {

    public static final String TUI_TITLE_KEY = "tui.title";

    public static final String TUI_TRANSCRIPT_TITLE_KEY = "tui.transcript.title";

    public static final String TUI_PROMPT_TITLE_KEY = "tui.prompt.title";

    public static final String TUI_EMPTY_TRANSCRIPT_KEY = "tui.empty.transcript";

    public static final String TUI_USER_LABEL_KEY = "tui.user.label";

    public static final String TUI_ASSISTANT_LABEL_KEY = "tui.assistant.label";

    public static final String TUI_ERROR_LABEL_KEY = "tui.error.label";

    public static final String TUI_TOOL_LABEL_KEY = "tui.tool.label";

    private final MessageSource messageSource;

    private final CodegeistLocaleService localeService;

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, localeService.currentLocale());
    }
}
