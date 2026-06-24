package ai.codegeist.app.tool;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.CodegeistShellToolConfig;
import ai.codegeist.app.config.ToolsConfig;
import ai.codegeist.app.config.ToolsRootElement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Component;

/** Resolves the host-side command wrapper used by {@code codegeist_shell}. */
@Component
@RequiredArgsConstructor
final class CodegeistShellToolSettings {

    private static final List<String> DEFAULT_COMMAND_PREFIX =
            SystemUtils.IS_OS_WINDOWS ? List.of("cmd.exe", "/c") : List.of("sh", "-lc");

    private final CodegeistConfig config;

    List<String> commandPrefix() {
        return config.rootElement(ToolsRootElement.class)
                .map(ToolsRootElement::getConfig)
                .map(ToolsConfig::getCodegeistShell)
                .map(CodegeistShellToolConfig::getCommandPrefix)
                .filter(prefix -> !prefix.isEmpty())
                .orElse(DEFAULT_COMMAND_PREFIX);
    }

    long timeoutSeconds(Long requestedTimeoutSeconds) {
        if (requestedTimeoutSeconds != null && requestedTimeoutSeconds > 0) {
            return requestedTimeoutSeconds;
        }

        return config.rootElement(ToolsRootElement.class)
                .map(ToolsRootElement::getConfig)
                .map(ToolsConfig::getCodegeistShell)
                .map(CodegeistShellToolConfig::getDefaultTimeoutSeconds)
                .orElse(CodegeistShellToolConfig.DEFAULT_TIMEOUT_SECONDS);
    }
}
