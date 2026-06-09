package ai.codegeist.app.config;

import ai.codegeist.app.CodegeistApplication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodegeistConfig {

    public static final String CONFIGURATION_PREFIX = CodegeistApplication.APP_NAME;
    public static final String NO_PROVIDER_MESSAGE = "No provider configured in Codegeist config";

    @Valid
    final List<@Valid CodegeistConfigRootElement<? extends CodegeistConfigElement>> rootElements = new ArrayList<>();

    public Optional<ProviderConfig> defaultProvider() {
        return rootElement(ProvidersRootElement.class)
                .flatMap(ProvidersRootElement::defaultProvider);
    }

    public <T extends CodegeistConfigRootElement<? extends CodegeistConfigElement>> Optional<T> rootElement(
            Class<T> rootElementType) {
        return rootElements.stream()
                .filter(rootElementType::isInstance)
                .map(rootElementType::cast)
                .findFirst();
    }
}
