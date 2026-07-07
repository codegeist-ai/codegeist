package ai.codegeist.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

class CodegeistSpringAppPropertiesTest {

    @Test
    void initializesNestedPropertyDefaults() {
        CodegeistSpringAppProperties properties = new CodegeistSpringAppProperties();

        assertThat(properties.getSession().getDirectory()).isEqualTo(CodegeistSpringAppProperties.DEFAULT_SESSION_DIRECTORY);
        assertThat(properties.getSession().getStoreFile()).isEqualTo(CodegeistSpringAppProperties.DEFAULT_SESSION_STORE_FILE);
        assertThat(properties.getLocale()).isNull();
    }

    @Test
    void rejectsNullNestedPropertyHolders() {
        CodegeistSpringAppProperties properties = new CodegeistSpringAppProperties();

        assertThatNullPointerException().isThrownBy(() -> properties.setSession(null));
    }
}
