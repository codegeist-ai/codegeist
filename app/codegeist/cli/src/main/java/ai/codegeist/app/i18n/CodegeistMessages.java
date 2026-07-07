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

    public static final String TUI_QUIT_HINT_KEY = "tui.quit.hint";

    private final MessageSource messageSource;

    private final CodegeistLocaleService localeService;

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, localeService.currentLocale());
    }
}
