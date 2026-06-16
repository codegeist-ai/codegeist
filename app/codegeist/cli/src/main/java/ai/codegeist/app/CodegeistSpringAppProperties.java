package ai.codegeist.app;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConfigurationProperties(prefix = CodegeistSpringAppProperties.CONFIGURATION_PREFIX)
public class CodegeistSpringAppProperties {

    public static final String CONFIGURATION_PREFIX = CodegeistApplication.APP_NAME;
    public static final String SESSION_DIRECTORY_PROPERTY = CONFIGURATION_PREFIX + ".session.directory";
    public static final String SESSION_STORE_FILE_PROPERTY = CONFIGURATION_PREFIX + ".session.store-file";
    public static final String DEFAULT_SESSION_DIRECTORY = ".codegeist";
    public static final String DEFAULT_SESSION_STORE_FILE = "session.json";

    @Getter
    private Session session = new Session();

    public void setSession(Session session) {
        this.session = session == null ? new Session() : session;
    }

    @Setter
    public static class Session {

        private String directory = DEFAULT_SESSION_DIRECTORY;
        private String storeFile = DEFAULT_SESSION_STORE_FILE;

        public String getDirectory() {
            return StringUtils.hasText(directory) ? directory : DEFAULT_SESSION_DIRECTORY;
        }

        public String getStoreFile() {
            return StringUtils.hasText(storeFile) ? storeFile : DEFAULT_SESSION_STORE_FILE;
        }
    }
}
