package ai.codegeist.app.session;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

@Component
public class SessionStoreObjectMapper extends ObjectMapper {

    public SessionStoreObjectMapper() {
        registerModule(new JavaTimeModule());
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }
}
