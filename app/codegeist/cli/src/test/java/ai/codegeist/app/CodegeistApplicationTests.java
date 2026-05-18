package ai.codegeist.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.shell.interactive.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.shell.core.autoconfigure.SpringShellAutoConfiguration"
    }
)
class CodegeistApplicationTests {

    @Test
    void contextLoads() {
    }
}
