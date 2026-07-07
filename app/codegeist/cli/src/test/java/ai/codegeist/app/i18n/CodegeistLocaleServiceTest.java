package ai.codegeist.app.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistSpringAppProperties;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class CodegeistLocaleServiceTest {

    @Test
    void fallsBackToDefaultLocale() {
        CodegeistLocaleService localeService = new CodegeistLocaleService(new CodegeistSpringAppProperties());

        assertThat(localeService.currentLocale()).isEqualTo(Locale.getDefault());
    }

    @Test
    void returnsConfiguredLocale() {
        CodegeistSpringAppProperties properties = new CodegeistSpringAppProperties();
        properties.setLocale(Locale.GERMANY);
        CodegeistLocaleService localeService = new CodegeistLocaleService(properties);

        assertThat(localeService.currentLocale()).isEqualTo(Locale.GERMANY);
    }
}
