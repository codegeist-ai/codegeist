package ai.codegeist.app.tool;

import ai.codegeist.app.config.CodegeistConfig;
import ai.codegeist.app.config.WorkspaceConfig;
import ai.codegeist.app.config.WorkspaceRootElement;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the global text charset used by local file tools. */
@Component
@RequiredArgsConstructor
final class CodegeistFileEncoding {

    static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final CodegeistConfig config;

    Charset currentCharset() {
        return config.rootElement(WorkspaceRootElement.class)
                .map(WorkspaceRootElement::getConfig)
                .map(WorkspaceConfig::getEncoding)
                .orElse(DEFAULT_CHARSET);
    }
}
