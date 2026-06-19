package ai.codegeist.app.config;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProvidersConfig extends CodegeistConfigKeyedListElement<ProviderConfig> {

    @Override
    protected String key(ProviderConfig provider) {
        return provider.getType();
    }

    public Optional<ProviderConfig> defaultProvider() {
        for (ProviderConfig configuredProvider : getElements()) {
            if (configuredProvider != null) {
                log.debug("Selected first configured provider type {}", configuredProvider.getType());
                return Optional.of(configuredProvider);
            }
        }

        return Optional.empty();
    }
}
