package ai.codegeist.app.config;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodegeistConfigRootElementParserService {

    static final String UNSUPPORTED_ROOT_ELEMENT_PREFIX = "Unsupported Codegeist config root element: ";

    private final List<CodegeistConfigRootElement<? extends CodegeistConfigElement>> rootElementParsers;

    private final CodegeistConfigYamlMapper yamlMapper;

    public CodegeistConfigRootElement<? extends CodegeistConfigElement> parseRootElement(@NonNull String rootName,
            @NonNull JsonNode source) {
        return rootElementParsers.stream()
                .filter(candidate -> candidate.rootName().equals(rootName))
                .findFirst()
                .orElseThrow(() -> new CodegeistConfigValidationException(UNSUPPORTED_ROOT_ELEMENT_PREFIX + rootName))
                .parse(source, yamlMapper);
    }
}
