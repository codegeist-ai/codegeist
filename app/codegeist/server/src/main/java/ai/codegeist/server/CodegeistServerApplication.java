package ai.codegeist.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Codegeist Cloud server entrypoint.
 *
 * <p>This application is intentionally separate from the local CLI application in
 * {@code app/codegeist/cli}. It will grow into the hosted control plane for
 * authenticated model access, artifact storage, and sync workflows. The current
 * slice proves that the server module boots, exposes a local health API,
 * binds static external OAuth2/OIDC provider configuration, protects Codegeist API
 * routes, and exposes the first authenticated identity API. It does not call
 * hosted LLMs, perform browser login, issue Codegeist API tokens, or connect to
 * object storage.
 */
@SpringBootApplication
public class CodegeistServerApplication {

    public static final String APP_NAME = "codegeist-server";
    public static final String CONFIGURATION_PREFIX = "codegeist";

    public static void main(String[] args) {
        SpringApplication.run(CodegeistServerApplication.class, args);
    }
}
