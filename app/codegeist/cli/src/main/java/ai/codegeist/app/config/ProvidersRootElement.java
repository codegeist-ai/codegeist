package ai.codegeist.app.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProvidersRootElement extends CodegeistConfigRootElement<ProviderConfig> {

    public static final String ROOT_NAME = "provider";

    private static final Map<String, Class<? extends ProviderConfig>> PROVIDER_CLASSES = Map.of(
            OllamaProviderConfig.PROVIDER_TYPE, OllamaProviderConfig.class,
            OpenAiProviderConfig.PROVIDER_TYPE, OpenAiProviderConfig.class);

    @Valid
    private final List<@Valid ProviderConfig> providers = new ArrayList<>();

    @Override
    public ProvidersRootElement parse(JsonNode source, ObjectMapper objectMapper) {
        ProvidersRootElement root = new ProvidersRootElement();
        root.providers.addAll(convertElements(source, objectMapper));
        return root;
    }

    @Override
    public Object toYamlValue() {
        return getProviders();
    }

    @Override
    public String rootName() {
        return ROOT_NAME;
    }

    public Map<String, ProviderConfig> getProviders() {
        Map<String, ProviderConfig> providerMap = new LinkedHashMap<>();
        for (ProviderConfig provider : providers) {
            if (provider != null) {
                providerMap.put(provider.getType(), provider);
            }
        }
        return providerMap;
    }

    public Optional<ProviderConfig> defaultProvider() {
        for (ProviderConfig configuredProvider : providers) {
            if (configuredProvider != null) {
                log.debug("Selected first configured provider type {}", configuredProvider.getType());
                return Optional.of(configuredProvider);
            }
        }

        return Optional.empty();
    }

    @Override
    protected Map<String, Class<? extends ProviderConfig>> elementClasses() {
        return PROVIDER_CLASSES;
    }

}
