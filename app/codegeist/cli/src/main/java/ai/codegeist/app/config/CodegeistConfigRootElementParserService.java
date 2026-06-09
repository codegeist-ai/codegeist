package ai.codegeist.app.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CodegeistConfigRootElementParserService {

    static final String UNSUPPORTED_ROOT_ELEMENT_PREFIX = "Unsupported Codegeist config root element: ";

    @Autowired
    private List<CodegeistConfigRootElement<? extends CodegeistConfigElement>> rootElementParsers = new ArrayList<>();

    @Autowired
    @Qualifier(CodegeistYamlConfiguration.CODEGEIST_YAML_OBJECT_MAPPER_BEAN)
    private ObjectMapper yamlMapper;

    public CodegeistConfigRootElement<? extends CodegeistConfigElement> parseRootElement(@NonNull String rootName,
            @NonNull JsonNode source) {
        return rootElementParsers.stream()
                .filter(candidate -> candidate.rootName().equals(rootName))
                .findFirst()
                .orElseThrow(() -> new CodegeistConfigValidationException(UNSUPPORTED_ROOT_ELEMENT_PREFIX + rootName))
                .parse(source, yamlMapper);
    }
}
