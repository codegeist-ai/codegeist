package ai.codegeist.app.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class CodegeistConfigService {

    static final String SHOW_CONFIG_COMMAND = "--show-config";

    public static final String CONFIG_PROPERTY = CodegeistConfig.CONFIGURATION_PREFIX + ".config";

    static final String VALIDATION_ERROR_PREFIX = "Invalid Codegeist config file: ";

    @Getter
    @Autowired
    @Qualifier(CodegeistConfig.SPRING_BOUND_CONFIG_BEAN)
    private CodegeistConfig springBoundConfig;

    @Autowired
    private Validator validator;

    @Autowired
    @Qualifier(CodegeistYamlConfiguration.CODEGEIST_YAML_OBJECT_MAPPER_BEAN)
    private ObjectMapper yamlMapper;

    @Autowired
    private CodegeistYamlExpressionEvaluator expressionEvaluator;

    @Value("${" + CONFIG_PROPERTY + ":}")
    private String configPath;

    @Bean
    @Primary
    public CodegeistConfig primaryCodegeistConfig() {
        log.debug("Creating primary Codegeist config bean from Spring-bound config");
        return springBoundConfig;
    }

    public CodegeistConfig getCurrentConfig() {
        if (StringUtils.hasText(configPath)) {
            log.debug("Using Codegeist config from system property {}", CONFIG_PROPERTY);
            return loadConfig(configPath);
        }

        log.debug("Using Spring-bound Codegeist config");
        return primaryCodegeistConfig();
    }

    @SneakyThrows(IOException.class)
    public CodegeistConfig loadConfig(String configPath) {
        log.debug("Loading Codegeist config file: {}", configPath);
        JsonNode rawTree = yamlMapper.readTree(Path.of(configPath).toFile());
        JsonNode sourceTree = rawTree == null ? yamlMapper.createObjectNode() : rawTree;
        JsonNode evaluatedTree = expressionEvaluator.evaluate(sourceTree, configPath);
        CodegeistConfig loadedConfig = yamlMapper.readerFor(CodegeistConfig.class)
                .readValue(evaluatedTree);
        validateConfig(loadedConfig, configPath);
        log.debug("Loaded valid Codegeist config file: {}", configPath);
        return loadedConfig;
    }

    @SneakyThrows(JsonProcessingException.class)
    public String toYaml(CodegeistConfig codegeistConfig) {
        log.debug("Rendering Codegeist config as YAML");
        return yamlMapper.writeValueAsString(codegeistConfig == null ? new CodegeistConfig() : codegeistConfig);
    }

    @Command(name = SHOW_CONFIG_COMMAND, description = "Print the current Codegeist config")
    public void showConfig(CommandContext context) {
        log.debug("Printing current Codegeist config");
        // Native/release smokes assert stdout is YAML-only; keep labels and logs out
        // of this path.
        // Route through the global config policy so `-Dcodegeist.config=...` is
        // shared by CLI commands instead of being command-specific.
        context.outputWriter().print(toYaml(getCurrentConfig()));
        context.outputWriter().flush();
    }

    private void validateConfig(CodegeistConfig loadedConfig, String configPath) {
        // Jackson does not run Bean Validation; keep this paired with
        // CodegeistConfig constraints.
        Set<ConstraintViolation<CodegeistConfig>> violations = validator.validate(loadedConfig);
        if (violations.isEmpty()) {
            log.debug("Validated Codegeist config file: {}", configPath);
            return;
        }

        String message = violations.stream()
                .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining("; "));
        log.debug("Codegeist config validation failed for {}: {}", configPath, message);
        throw new CodegeistConfigValidationException(VALIDATION_ERROR_PREFIX + configPath + ": " + message);
    }
}
