package ai.codegeist.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal unauthenticated liveness endpoint for the first server bootstrap.
 *
 * <p>Keep this endpoint deliberately small until later T008 tasks define auth,
 * tenancy, storage, model access, and operational health/readiness semantics.
 */
@RestController
class HealthController {

    static final String HEALTH_PATH = "/health";
    static final String STATUS_OK = "ok";

    @GetMapping(HEALTH_PATH)
    HealthResponse health() {
        return new HealthResponse(STATUS_OK);
    }

    record HealthResponse(String status) {
    }
}
