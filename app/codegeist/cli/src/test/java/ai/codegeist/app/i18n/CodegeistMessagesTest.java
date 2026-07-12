package ai.codegeist.app.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistSpringAppProperties;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.support.StaticMessageSource;

class CodegeistMessagesTest {

    @Test
    void resolvesDefaultMessagesFromResourceBundle() {
        CodegeistMessages messages = messages();

        assertThat(messages.get(CodegeistMessages.TUI_TITLE_KEY)).isEqualTo("Codegeist");
        assertThat(messages.get(CodegeistMessages.TUI_TRANSCRIPT_TITLE_KEY)).isEqualTo("Transcript");
        assertThat(messages.get(CodegeistMessages.TUI_PROMPT_TITLE_KEY)).isEqualTo("Prompt");
        assertThat(messages.get(CodegeistMessages.TUI_EMPTY_TRANSCRIPT_KEY))
                .isEqualTo("Enter a prompt below. Press Ctrl-Q to quit.");
        assertThat(messages.get(CodegeistMessages.TUI_USER_LABEL_KEY)).isEqualTo("You");
        assertThat(messages.get(CodegeistMessages.TUI_ASSISTANT_LABEL_KEY)).isEqualTo("Codegeist");
        assertThat(messages.get(CodegeistMessages.TUI_ERROR_LABEL_KEY)).isEqualTo("Error");
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
