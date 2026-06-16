package ai.codegeist.app.config;

import ai.codegeist.app.CommandOutputService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodegeistConfigService {

    static final String SHOW_CONFIG_COMMAND = "--show-config";

    public static final String CONFIG_PROPERTY = CodegeistConfig.CONFIGURATION_PREFIX + ".config";

    static final String VALIDATION_ERROR_PREFIX = "Invalid Codegeist config file: ";
    static final String ROOT_OBJECT_MESSAGE = "Codegeist config must be a YAML object";

    private final Validator validator;

    private final CodegeistConfigYamlMapper yamlMapper;

    private final CodegeistYamlExpressionEvaluator expressionEvaluator;

    private final CodegeistConfigRootElementParserService rootElementParserService;

    private final CommandOutputService outputService;

    @Value("${" + CONFIG_PROPERTY + ":}")
    private String configPath;

    @Bean
    @Primary
    public CodegeistConfig loadCurrentConfig() {
        if (StringUtils.hasText(configPath)) {
            log.debug("Using Codegeist config from system property {}", CONFIG_PROPERTY);
            return loadConfig(configPath);
        }

        log.debug("Using empty default Codegeist config");
        return new CodegeistConfig();
    }

    @SneakyThrows(IOException.class)
    public CodegeistConfig loadConfig(String configPath) {
        log.debug("Loading Codegeist config file: {}", configPath);
        JsonNode sourceTree = yamlMapper.readConfigSourceTree(Path.of(configPath));
        JsonNode evaluatedTree = expressionEvaluator.evaluate(sourceTree, configPath);
        CodegeistConfig loadedConfig = parseConfig(evaluatedTree);
        validateConfig(loadedConfig, configPath);
        log.debug("Loaded valid Codegeist config file: {}", configPath);
        return loadedConfig;
    }

    @SneakyThrows(JsonProcessingException.class)
    public String toYaml(CodegeistConfig codegeistConfig) {
        log.debug("Rendering Codegeist config as YAML");
        return yamlMapper.writeConfig(codegeistConfig);
    }

    @Command(name = SHOW_CONFIG_COMMAND, description = "Print the current Codegeist config")
    public void showConfig(CommandContext context) {
        log.debug("Printing current Codegeist config");
        // Native/release smokes assert stdout is YAML-only; keep labels and logs out
        // of this path.
        // Route through the global config policy so `-Dcodegeist.config=...` is
        // shared by CLI commands instead of being command-specific.
        outputService.print(context, toYaml(loadCurrentConfig()));
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

    private CodegeistConfig parseConfig(JsonNode source) {
        CodegeistConfig config = new CodegeistConfig();
        if (source == null || source.isNull()) {
            return config;
        }
        if (!(source instanceof ObjectNode objectNode)) {
            throw new CodegeistConfigValidationException(ROOT_OBJECT_MESSAGE);
        }

        for (Map.Entry<String, JsonNode> field : objectNode.properties()) {
            config.rootElements.add(rootElementParserService.parseRootElement(field.getKey(), field.getValue()));
        }
        return config;
    }
}
