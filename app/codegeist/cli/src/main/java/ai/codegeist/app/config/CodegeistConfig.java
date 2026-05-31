package ai.codegeist.app.config;

import ai.codegeist.app.CodegeistApplication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Slf4j
@Component(CodegeistConfig.SPRING_BOUND_CONFIG_BEAN)
@ConfigurationProperties(prefix = CodegeistConfig.CONFIGURATION_PREFIX)
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Validated
public class CodegeistConfig {

    public static final String CONFIGURATION_PREFIX = CodegeistApplication.APP_NAME;
    public static final String SPRING_BOUND_CONFIG_BEAN = "springBoundCodegeistConfig";

    @Valid
    private Map<@NotBlank String, @Valid ProviderConfig> provider = new LinkedHashMap<>();

    public CodegeistConfig() {
        log.debug("Creating Codegeist config");
    }

    public CodegeistConfig merge(CodegeistConfig override) {
        CodegeistConfig merged = new CodegeistConfig();
        merged.setProvider(copyProviderMap(provider));

        if (override == null || override.getProvider() == null) {
            return merged;
        }

        // T006_01 source precedence: later config sources add providers or
        // override fields by id.
        override.getProvider().forEach((providerId, overrideProvider) -> {
            ProviderConfig baseProvider = merged.getProvider().get(providerId);
            merged.getProvider().put(providerId, mergeProvider(baseProvider, overrideProvider));
        });

        return merged;
    }

    private Map<String, ProviderConfig> copyProviderMap(Map<String, ProviderConfig> source) {
        Map<String, ProviderConfig> copy = new LinkedHashMap<>();
        if (source == null) {
            return copy;
        }

        source.forEach((providerId, providerConfig) -> copy.put(providerId, copyProvider(providerConfig)));
        return copy;
    }

    private ProviderConfig mergeProvider(ProviderConfig baseProvider, ProviderConfig overrideProvider) {
        if (baseProvider == null) {
            return copyProvider(overrideProvider);
        }

        return baseProvider.merge(overrideProvider);
    }

    private ProviderConfig copyProvider(ProviderConfig providerConfig) {
        return providerConfig == null ? null : providerConfig.merge(null);
    }
}
