package ai.codegeist.app;

import lombok.NonNull;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.stereotype.Service;

@Service
public class CommandOutputService {

    public void print(@NonNull CommandContext context, @NonNull String text) {
        context.outputWriter().print(text);
        context.outputWriter().flush();
    }
}
