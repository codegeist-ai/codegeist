package ai.codegeist.server.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "codegeist.auth.providers.authentik.type=oidc",
    "codegeist.auth.providers.authentik.issuer-uri=https://auth.example.net/application/o/codegeist/",
    "codegeist.auth.providers.authentik.client-id=codegeist",
    "codegeist.auth.providers.keycloak.type=oidc",
    "codegeist.auth.providers.keycloak.issuer-uri=https://sso.example.com/realms/codegeist",
    "codegeist.auth.providers.keycloak.client-id=codegeist"
})
class CodegeistAuthPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    private CodegeistAuthProperties authProperties;

    @Test
    void bindsMultipleStaticOidcProviders() {
        assertThat(authProperties.getProviders()).containsOnlyKeys("authentik", "keycloak");
        assertThat(authProperties.getProviders().get("authentik").getType())
                .isEqualTo(AuthProviderProperties.PROVIDER_TYPE_OIDC);
        assertThat(authProperties.getProviders().get("authentik").getIssuerUri())
                .isEqualTo("https://auth.example.net/application/o/codegeist/");
        assertThat(authProperties.getProviders().get("authentik").getClientId()).isEqualTo("codegeist");
    }

    @ParameterizedTest
    @CsvSource({
        "authentik, saml, https://auth.example.net/application/o/codegeist/, codegeist, must be oidc",
        "authentik, oidc, ' ', codegeist, must not be blank",
        "authentik, oidc, https://auth.example.net/application/o/codegeist/, ' ', must not be blank",
        "Bad_Id, oidc, https://auth.example.net/application/o/codegeist/, codegeist, provider ids must match"
    })
    void rejectsInvalidProviderDefinitions(String providerId, String type, String issuerUri, String clientId,
            String expectedMessage) {
        CodegeistAuthProperties properties = new CodegeistAuthProperties();
        AuthProviderProperties provider = new AuthProviderProperties();
        provider.setType(type);
        provider.setIssuerUri(issuerUri);
        provider.setClientId(clientId);
        properties.setProviders(Map.of(providerId, provider));

        Set<ConstraintViolation<CodegeistAuthProperties>> violations = validator.validate(properties);

        assertThat(messages(violations)).anySatisfy(message -> assertThat(message).contains(expectedMessage));
    }

    private Set<String> messages(Set<ConstraintViolation<CodegeistAuthProperties>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }
}
