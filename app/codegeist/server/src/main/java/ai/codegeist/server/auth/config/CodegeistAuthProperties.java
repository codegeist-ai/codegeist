package ai.codegeist.server.auth.config;

import ai.codegeist.server.CodegeistServerApplication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * Spring application properties for Codegeist Cloud authentication.
 *
 * <p>Provider ids are deployment-local names used in login URLs, logs, and
 * metadata records. The server owns this list; CLI configuration only selects a
 * Codegeist server URL and does not decide which upstream identity providers are
 * available.
 */
@Validated
@Component
@ConfigurationProperties(prefix = CodegeistAuthProperties.CONFIGURATION_PREFIX)
public class CodegeistAuthProperties {

    public static final String CONFIGURATION_PREFIX = CodegeistServerApplication.CONFIGURATION_PREFIX + ".auth";
    public static final String PROVIDER_ID_PATTERN = "[a-z][a-z0-9-]*";
    public static final String PROVIDER_ID_MESSAGE = "provider ids must match " + PROVIDER_ID_PATTERN;

    @Valid
    private Map<String, AuthProviderProperties> providers = new LinkedHashMap<>();

    public Map<String, AuthProviderProperties> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, AuthProviderProperties> providers) {
        this.providers = providers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(providers);
    }

    @AssertTrue(message = PROVIDER_ID_MESSAGE)
    public boolean isProviderIdsValid() {
        return providers.keySet().stream()
                .allMatch(providerId -> StringUtils.hasText(providerId) && providerId.matches(PROVIDER_ID_PATTERN));
    }
}
