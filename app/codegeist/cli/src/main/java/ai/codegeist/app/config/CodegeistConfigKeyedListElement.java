package ai.codegeist.app.config;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * Base config element for roots stored as lists but rendered as keyed YAML objects.
 *
 * <p>Direct {@code codegeist.yml} uses provider-style objects for roots such as
 * {@code provider:} and {@code mcp:}. Runtime code still needs ordered lists so it
 * can preserve declaration order without exposing a map API. Subclasses define the
 * YAML key for each element; the backing list is intentionally returned directly so
 * the parser can populate the model without constructor boilerplate. The YAML mapper
 * asks this element for a transient keyed object when direct config output is
 * rendered.
 */
public abstract class CodegeistConfigKeyedListElement<T extends CodegeistConfigElement> extends CodegeistConfigElement {

    @Getter
    @Valid
    private final List<@Valid T> elements = new ArrayList<>();

    protected abstract String key(T element);

    Map<String, T> keyedElements() {
        Map<String, T> yamlElements = new LinkedHashMap<>();
        for (T element : elements) {
            if (element != null) {
                yamlElements.put(key(element), element);
            }
        }
        return yamlElements;
    }
}
