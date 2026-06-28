package ai.codegeist.app;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.shell.core.NonInteractiveShellRunner;
import org.springframework.shell.core.ShellRunner;
import org.springframework.shell.core.command.CommandParser;
import org.springframework.shell.core.command.CommandRegistry;

/**
 * Keeps command-argument execution on Spring Shell's non-interactive runner.
 *
 * <p>Adding {@code spring-shell-jline} contributes terminal UI infrastructure and
 * can also make an interactive runner available. Codegeist currently dispatches
 * commands such as {@code --version}, {@code --show-config}, and {@code tui} from
 * process arguments, so this primary runner preserves that contract while
 * {@code spring.shell.interactive.enabled=false} remains configured.</p>
 */
@Configuration(proxyBeanMethods = false)
class CodegeistShellRunnerConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "spring.shell.interactive", name = "enabled", havingValue = "false")
    ShellRunner codegeistNonInteractiveShellRunner(CommandParser commandParser, CommandRegistry commandRegistry) {
        return new NonInteractiveShellRunner(commandParser, commandRegistry);
    }
}
