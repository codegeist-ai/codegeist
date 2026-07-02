package ai.codegeist.server.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local-authentik")
class LocalAuthentikProfileTest {

    @Autowired
    private CodegeistAuthProperties authProperties;

    @Test
    void localProfileConfiguresAuthentikAsGenericOidcProvider() {
        AuthProviderProperties authentik = authProperties.getProviders().get("authentik");

        assertThat(authentik).isNotNull();
        assertThat(authentik.getType()).isEqualTo(AuthProviderProperties.PROVIDER_TYPE_OIDC);
        assertThat(authentik.getIssuerUri()).isEqualTo("http://localhost:9000/application/o/codegeist/");
        assertThat(authentik.getClientId()).isEqualTo("codegeist");
        assertThat(authentik.getClientSecret()).isNull();
    }
}
