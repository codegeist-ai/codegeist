package ai.codegeist.server.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.codegeist.server.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CloudIdentityControllerTest {

    private static final String USER_ID = "user-123";
    private static final String ACCOUNT_ID = "account-123";
    private static final String ISSUER = "https://codegeist.cloud";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get(HealthController.HEALTH_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HealthController.STATUS_OK));
    }

    @Test
    void rejectsUnauthenticatedIdentityRequests() throws Exception {
        mockMvc.perform(get(CloudIdentityController.ME_PATH))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsIdentityFromAuthenticatedTokenClaims() throws Exception {
        mockMvc.perform(get(CloudIdentityController.ME_PATH).with(jwt().jwt(jwt -> jwt
                        .subject(USER_ID)
                        .claim(CloudIdentityController.ACCOUNT_ID_CLAIM, ACCOUNT_ID)
                        .claim(JwtClaimNames.ISS, ISSUER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.accountId").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.issuer").value(ISSUER));
    }

    @Test
    void ignoresClientSuppliedIdentityHeaders() throws Exception {
        mockMvc.perform(get(CloudIdentityController.ME_PATH)
                        .header("x-codegeist-user-id", "spoofed-user")
                        .header("x-codegeist-account-id", "spoofed-account")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(USER_ID)
                                .claim(CloudIdentityController.ACCOUNT_ID_CLAIM, ACCOUNT_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.accountId").value(ACCOUNT_ID));
    }

    @Test
    void rejectsAuthenticatedTokensWithoutCodegeistAccountClaim() throws Exception {
        mockMvc.perform(get(CloudIdentityController.ME_PATH).with(jwt().jwt(jwt -> jwt.subject(USER_ID))))
                .andExpect(status().isForbidden());
    }
}
