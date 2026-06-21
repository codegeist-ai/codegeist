package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for config entries that use a required {@code type} discriminator.
 *
 * <p>Concrete roots such as provider and MCP client entries keep their public YAML
 * key as an id, while this field carries the implementation or transport selector
 * parsed from the nested {@code type} property. The generic type keeps callers tied
 * to the concrete enum or string contract owned by each config family.
 */
public abstract class CodegeistTypedConfigElement<T> extends CodegeistConfigElement {

    public static final String TYPE_PROPERTY = "type";

    /**
     * Required discriminator copied from direct {@code codegeist.yml} entries.
     */
    @Getter
    @Setter
    @NotNull
    @JsonProperty(TYPE_PROPERTY)
    private T type;
}
