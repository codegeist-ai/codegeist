package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CodegeistConfigService {

    static final String SHOW_CONFIG_COMMAND = "--show-config";

    static final String LOAD_ERROR_PREFIX = "Failed to load Codegeist config file: ";
    static final String WRITE_ERROR_MESSAGE = "Failed to render Codegeist config as YAML";
    static final String VALIDATION_ERROR_PREFIX = "Invalid Codegeist config file: ";

    @Getter
    @Autowired
    @Qualifier(CodegeistConfig.SPRING_BOUND_CONFIG_BEAN)
    private CodegeistConfig springBoundConfig;

    @Autowired
    private Validator validator;

    // Keep direct codegeist.yml wrapper-free; see
    // docs/developer/architecture/provider-configuration.md and scripts/tests/native-smoke.sh.
    private final ObjectMapper yamlMapper = new ObjectMapper(YAMLFactory.builder()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .build())
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

    @Bean
    @Primary
    public CodegeistConfig mergedCodegeistConfig() {
        log.debug("Creating primary merged Codegeist config bean from Spring-bound config");
        return springBoundConfig;
    }

    public CodegeistConfig loadConfig(String configPath) {
        try {
            log.debug("Loading Codegeist config file: {}", configPath);
            CodegeistConfig loadedConfig = yamlMapper.readValue(Path.of(configPath).toFile(),
                    CodegeistConfig.class);
            validateConfig(loadedConfig, configPath);
            log.debug("Loaded valid Codegeist config file: {}", configPath);
            return loadedConfig;
        } catch (IOException ex) {
            throw new IllegalStateException(LOAD_ERROR_PREFIX + configPath, ex);
        }
    }

    public String toYaml(CodegeistConfig codegeistConfig) {
        try {
            log.debug("Rendering merged Codegeist config as YAML");
            return yamlMapper.writeValueAsString(codegeistConfig);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(WRITE_ERROR_MESSAGE, ex);
        }
    }

    @Command(name = SHOW_CONFIG_COMMAND, description = "Print the merged Codegeist config")
    public void showConfig(CommandContext context) {
        log.debug("Printing merged Codegeist config");
        // Native/release smokes assert stdout is YAML-only; keep labels and logs out
        // of this path.
        // Route through the merged bean method so later source merging reaches the command.
        context.outputWriter().print(toYaml(mergedCodegeistConfig()));
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
