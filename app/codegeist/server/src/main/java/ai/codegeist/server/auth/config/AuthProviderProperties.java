package ai.codegeist.server.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Static external identity-provider configuration for one Codegeist Server.
 *
 * <p>The first auth slice supports only generic OIDC providers. This class stores
 * deployment-local access data used by later browser login endpoints; it does not
 * store provider tokens or Codegeist API tokens.
 */
public class AuthProviderProperties {

    public static final String PROVIDER_TYPE_OIDC = "oidc";

    static final String SUPPORTED_TYPE_MESSAGE = "must be oidc";

    @NotBlank
    @Pattern(regexp = PROVIDER_TYPE_OIDC, message = SUPPORTED_TYPE_MESSAGE)
    private String type;

    @NotBlank
    private String issuerUri;

    @NotBlank
    private String clientId;

    private String clientSecret;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
