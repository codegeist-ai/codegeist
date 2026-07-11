package ai.codegeist.server.api;

import java.net.URL;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * First authenticated Codegeist Cloud API endpoint.
 *
 * <p>The endpoint exposes only the minimum identity contract needed by later CLI
 * cloud-login and sync work: the Codegeist user id, the user's current personal
 * account id, and the token issuer when one is present. It intentionally derives
 * identity only from the authenticated security principal and ignores any
 * client-supplied {@code x-codegeist-*} headers.
 */
@RestController
class CloudIdentityController {

    static final String ME_PATH = "/api/v1/me";
    static final String ACCOUNT_ID_CLAIM = "codegeist_account_id";
    static final String MISSING_IDENTITY_CLAIM_MESSAGE = "Authenticated token is missing Codegeist identity claims";

    @GetMapping(ME_PATH)
    CloudIdentity me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || !StringUtils.hasText(jwt.getSubject())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MISSING_IDENTITY_CLAIM_MESSAGE);
        }

        String accountId = jwt.getClaimAsString(ACCOUNT_ID_CLAIM);
        if (!StringUtils.hasText(accountId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MISSING_IDENTITY_CLAIM_MESSAGE);
        }

        URL issuer = jwt.getIssuer();
        return new CloudIdentity(jwt.getSubject(), accountId, issuer == null ? null : issuer.toString());
    }

    record CloudIdentity(String userId, String accountId, String issuer) {
    }
}
