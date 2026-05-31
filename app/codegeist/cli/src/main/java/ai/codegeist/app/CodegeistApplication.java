package ai.codegeist.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodegeistApplication {

    public static final String APP_NAME = "codegeist";

    public static void main(String[] args) {
        SpringApplication.run(CodegeistApplication.class, args);
    }
}
