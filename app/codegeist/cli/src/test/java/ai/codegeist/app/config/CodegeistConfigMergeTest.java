package ai.codegeist.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CodegeistConfigMergeTest {

    private static final String OLLAMA_PROVIDER_ID = "ollama";
    private static final String OPENAI_PROVIDER_ID = "openai";
    private static final String BASE_PROVIDER_NAME = "Old Ollama";
    private static final String OVERRIDE_PROVIDER_NAME = "Ollama";
    private static final String NEW_PROVIDER_NAME = "OpenAI";

    @Test
    void configMergeAddsNewProviderEntries() {
        CodegeistConfig base = configWithProvider(OLLAMA_PROVIDER_ID, BASE_PROVIDER_NAME);
        CodegeistConfig override = configWithProvider(OPENAI_PROVIDER_ID, NEW_PROVIDER_NAME);

        CodegeistConfig merged = base.merge(override);

        assertThat(merged.getProvider()).containsOnlyKeys(OLLAMA_PROVIDER_ID, OPENAI_PROVIDER_ID);
        assertThat(merged.getProvider().get(OLLAMA_PROVIDER_ID).getName())
                .isEqualTo(BASE_PROVIDER_NAME);
        assertThat(merged.getProvider().get(OPENAI_PROVIDER_ID).getName()).isEqualTo(NEW_PROVIDER_NAME);
    }

    @Test
    void configMergeOverridesProviderFieldsWhenSet() {
        CodegeistConfig base = configWithProvider(OLLAMA_PROVIDER_ID, BASE_PROVIDER_NAME);
        CodegeistConfig override = configWithProvider(OLLAMA_PROVIDER_ID, OVERRIDE_PROVIDER_NAME);

        CodegeistConfig merged = base.merge(override);

        assertThat(merged.getProvider()).containsOnlyKeys(OLLAMA_PROVIDER_ID);
        assertThat(merged.getProvider().get(OLLAMA_PROVIDER_ID).getName()).isEqualTo(OVERRIDE_PROVIDER_NAME);
    }

    @Test
    void providerMergeKeepsBaseFieldsWhenOverrideFieldsAreNull() {
        ProviderConfig base = provider(BASE_PROVIDER_NAME);
        ProviderConfig override = new ProviderConfig();

        ProviderConfig merged = base.merge(override);

        assertThat(merged.getName()).isEqualTo(BASE_PROVIDER_NAME);
    }

    @Test
    void providerMergeCopiesBaseWhenOverrideIsNull() {
        ProviderConfig base = provider(BASE_PROVIDER_NAME);

        ProviderConfig merged = base.merge(null);

        assertThat(merged).isNotSameAs(base);
        assertThat(merged.getName()).isEqualTo(BASE_PROVIDER_NAME);
    }

    @Test
    void configMergeCopiesBaseWhenOverrideIsNull() {
        CodegeistConfig base = configWithProvider(OLLAMA_PROVIDER_ID, BASE_PROVIDER_NAME);
        ProviderConfig baseProvider = base.getProvider().get(OLLAMA_PROVIDER_ID);

        CodegeistConfig merged = base.merge(null);

        assertThat(merged).isNotSameAs(base);
        assertThat(merged.getProvider()).isNotSameAs(base.getProvider());
        assertThat(merged.getProvider().get(OLLAMA_PROVIDER_ID)).isNotSameAs(baseProvider);
        assertThat(merged.getProvider().get(OLLAMA_PROVIDER_ID).getName()).isEqualTo(BASE_PROVIDER_NAME);
    }

    @Test
    void configMergeDoesNotMutateOrShareSourceProviderInstances() {
        CodegeistConfig base = configWithProvider(OLLAMA_PROVIDER_ID, BASE_PROVIDER_NAME);
        CodegeistConfig override = configWithProvider(OLLAMA_PROVIDER_ID, OVERRIDE_PROVIDER_NAME);
        ProviderConfig baseProvider = base.getProvider().get(OLLAMA_PROVIDER_ID);
        ProviderConfig overrideProvider = override.getProvider().get(OLLAMA_PROVIDER_ID);

        CodegeistConfig merged = base.merge(override);

        assertThat(base.getProvider().get(OLLAMA_PROVIDER_ID).getName())
                .isEqualTo(BASE_PROVIDER_NAME);
        assertThat(override.getProvider().get(OLLAMA_PROVIDER_ID).getName())
                .isEqualTo(OVERRIDE_PROVIDER_NAME);
        assertThat(merged).isNotSameAs(base).isNotSameAs(override);
        assertThat(merged.getProvider()).isNotSameAs(base.getProvider())
                .isNotSameAs(override.getProvider());
        assertThat(merged.getProvider().get(OLLAMA_PROVIDER_ID)).isNotSameAs(baseProvider)
                .isNotSameAs(overrideProvider);
    }

    private CodegeistConfig configWithProvider(String providerId, String providerName) {
        CodegeistConfig config = new CodegeistConfig();
        config.getProvider().put(providerId, provider(providerName));
        return config;
    }

    private ProviderConfig provider(String providerName) {
        ProviderConfig provider = new ProviderConfig();
        provider.setName(providerName);
        return provider;
    }
}
