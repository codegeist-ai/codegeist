package ai.codegeist.app.tool;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/** JSON mapper dedicated to model-supplied local tool input payloads. */
@Component
final class CodegeistToolJsonMapper extends ObjectMapper {

    CodegeistToolJsonMapper() {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
