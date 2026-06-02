package ai.codegeist.app.config;

import ai.codegeist.app.CodegeistApplication;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
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

    @Autowired
    @Qualifier(CodegeistYamlConfiguration.CODEGEIST_YAML_OBJECT_MAPPER_BEAN)
    @JacksonInject(value = CodegeistYamlConfiguration.CODEGEIST_YAML_OBJECT_MAPPER_BEAN, useInput = OptBoolean.FALSE)
    @Getter(AccessLevel.NONE)
    private ObjectMapper providerConverterMapper;

    public void setProvider(Map<String, Object> provider) {
        this.provider = convertProviderMap(provider, Objects.requireNonNull(providerConverterMapper,
                "Codegeist YAML ObjectMapper must be injected before provider binding"));
    }

    private Map<String, ProviderConfig> convertProviderMap(Map<String, ?> source, ObjectMapper objectMapper) {
        Map<String, ProviderConfig> converted = new LinkedHashMap<>();
        if (source == null) {
            return converted;
        }

        source.forEach((providerId, providerConfig) -> converted.put(providerId,
                ProviderConfigJacksonConverter.convertValue(providerConfig, objectMapper)));
        return converted;
    }
}
