package ai.codegeist.app.tui;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.CodegeistSpringAppProperties;
import ai.codegeist.app.i18n.CodegeistLocaleService;
import ai.codegeist.app.i18n.CodegeistMessages;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.shell.jline.tui.component.view.control.BoxView;

class CodegeistTerminalUiTest {

    @Test
    void createsRootViewWithDrawFunction() {
        CodegeistTerminalUi terminalUi = new CodegeistTerminalUi(null, messages());

        BoxView rootView = terminalUi.createRootView();

        assertThat(rootView.isShowBorder()).isTrue();
        assertThat(rootView.getDrawFunction()).isNotNull();
    }

    private static CodegeistMessages messages() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage(CodegeistMessages.TUI_TITLE_KEY, new CodegeistLocaleService(new CodegeistSpringAppProperties())
                .currentLocale(), "Codegeist");
        return new CodegeistMessages(messageSource, new CodegeistLocaleService(new CodegeistSpringAppProperties()));
    }
}
