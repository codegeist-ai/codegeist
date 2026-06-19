package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CodegeistConfigYamlMapper extends ObjectMapper {

    public CodegeistConfigYamlMapper() {
        super(YAMLFactory.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .build());
        setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
        setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    public JsonNode readConfigSourceTree(Path configPath) throws IOException {
        JsonNode rawTree = readTree(configPath.toFile());
        return rawTree == null ? createObjectNode() : rawTree;
    }

    public String writeConfig(CodegeistConfig codegeistConfig) throws JsonProcessingException {
        CodegeistConfig config = codegeistConfig == null ? new CodegeistConfig() : codegeistConfig;
        Map<String, Object> roots = new LinkedHashMap<>();
        for (CodegeistConfigRootElement<? extends CodegeistConfigElement> rootElement : config.rootElements) {
            roots.put(rootElement.rootName(), yamlValue(rootElement.getConfig()));
        }
        return writeValueAsString(roots);
    }

    private Object yamlValue(CodegeistConfigElement config) {
        if (config instanceof CodegeistConfigKeyedListElement<?> keyedList) {
            return keyedList.keyedElements();
        }
        return config;
    }
}
