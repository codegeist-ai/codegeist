package ai.codegeist.app.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistSpringAppProperties;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

class CodegeistMessagesTest {

    @Test
    void resolvesDefaultMessagesFromResourceBundle() {
        CodegeistMessages messages = messages();

        assertThat(messages.get(CodegeistMessages.TUI_TITLE_KEY)).isEqualTo("Codegeist");
        assertThat(messages.get(CodegeistMessages.TUI_QUIT_HINT_KEY)).isEqualTo("Press Ctrl-Q to quit");
    }

    @Test
    void usesConfiguredLocaleForMessageLookup() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage(CodegeistMessages.TUI_TITLE_KEY, Locale.ENGLISH, "Codegeist");
        messageSource.addMessage(CodegeistMessages.TUI_TITLE_KEY, Locale.GERMANY, "Codegeist DE");
        CodegeistSpringAppProperties properties = new CodegeistSpringAppProperties();
        properties.setLocale(Locale.GERMANY);
        CodegeistMessages messages = new CodegeistMessages(messageSource, new CodegeistLocaleService(properties));

        assertThat(messages.get(CodegeistMessages.TUI_TITLE_KEY)).isEqualTo("Codegeist DE");
    }

    private static CodegeistMessages messages() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return new CodegeistMessages(messageSource, new CodegeistLocaleService(new CodegeistSpringAppProperties()));
    }
}
