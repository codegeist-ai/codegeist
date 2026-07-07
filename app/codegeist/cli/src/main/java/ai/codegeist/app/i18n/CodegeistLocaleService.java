package ai.codegeist.app.i18n;

import ai.codegeist.app.CodegeistSpringAppProperties;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Selects the locale used by Codegeist message resolution.
 *
 * <p>The locale is intentionally isolated from {@link CodegeistMessages} so future
 * commands or UI surfaces can share message lookup without duplicating locale
 * selection. If {@code codegeist.locale} is not configured, the service preserves
 * JVM default locale behavior.</p>
 */
@Service
@RequiredArgsConstructor
public class CodegeistLocaleService {

    private final CodegeistSpringAppProperties properties;

    public Locale currentLocale() {
        Locale configuredLocale = properties.getLocale();
        return configuredLocale == null ? Locale.getDefault() : configuredLocale;
    }
}
