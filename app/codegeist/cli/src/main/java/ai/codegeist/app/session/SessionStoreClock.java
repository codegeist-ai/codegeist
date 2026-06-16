package ai.codegeist.app.session;

import java.time.Clock;
import java.time.Instant;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class SessionStoreClock {

    private final Clock clock;

    public SessionStoreClock() {
        this(Clock.systemUTC());
    }

    public SessionStoreClock(@NonNull Clock clock) {
        this.clock = clock;
    }

    public Instant now() {
        return clock.instant();
    }
}
