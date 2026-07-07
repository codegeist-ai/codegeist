package ai.codegeist.app;

import static org.assertj.core.api.Assertions.assertThat;

import ai.codegeist.app.session.SessionStoreService;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
            CodegeistSpringAppProperties.SESSION_DIRECTORY_PROPERTY + "=custom-codegeist",
            CodegeistSpringAppProperties.SESSION_STORE_FILE_PROPERTY + "=custom-session.json"
    }
)
class CodegeistApplicationTests {

    @Autowired
    private SessionStoreService sessionStoreService;

    @Test
    void contextLoads() {
    }

    @Test
    void sessionStorePathUsesInjectedProperties() {
        assertThat(sessionStoreService.currentStorePath().toString())
                .endsWith(Path.of("custom-codegeist", "custom-session.json").toString());
    }
}
