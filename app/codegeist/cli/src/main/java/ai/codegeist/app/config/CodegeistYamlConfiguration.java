package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CodegeistYamlConfiguration {

    public static final String CODEGEIST_YAML_OBJECT_MAPPER_BEAN = "codegeistYamlObjectMapper";

    @Bean(CODEGEIST_YAML_OBJECT_MAPPER_BEAN)
    public ObjectMapper codegeistYamlObjectMapper() {
        // Keep direct codegeist.yml wrapper-free; see provider-configuration.md and native-smoke.sh.
        ObjectMapper objectMapper = new ObjectMapper(YAMLFactory.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .build())
                .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setInjectableValues(new InjectableValues.Std()
                .addValue(CODEGEIST_YAML_OBJECT_MAPPER_BEAN, objectMapper));
        return objectMapper;
    }
}
