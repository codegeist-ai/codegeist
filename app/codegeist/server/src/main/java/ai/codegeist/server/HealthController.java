package ai.codegeist.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal unauthenticated liveness endpoint for the server bootstrap.
 *
 * <p>This endpoint stays public even after Codegeist API routes are protected. Keep
 * it deliberately small until later T008 tasks define operational
 * health/readiness semantics beyond process liveness.
 */
@RestController
public class HealthController {

    public static final String HEALTH_PATH = "/health";
    public static final String STATUS_OK = "ok";

    @GetMapping(HEALTH_PATH)
    HealthResponse health() {
        return new HealthResponse(STATUS_OK);
    }

    record HealthResponse(String status) {
    }
}
