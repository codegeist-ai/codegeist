package ai.codegeist.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Codegeist Cloud server entrypoint.
 *
 * <p>This application is intentionally separate from the local CLI application in
 * {@code app/codegeist/cli}. It will grow into the hosted control plane for
 * authenticated model access, artifact storage, and sync workflows. The first
 * slice only proves that the server module boots and exposes a local health API;
 * it does not call hosted LLMs, configure identity, or connect to object storage.
 */
@SpringBootApplication
public class CodegeistServerApplication {

    public static final String APP_NAME = "codegeist-server";

    public static void main(String[] args) {
        SpringApplication.run(CodegeistServerApplication.class, args);
    }
}
