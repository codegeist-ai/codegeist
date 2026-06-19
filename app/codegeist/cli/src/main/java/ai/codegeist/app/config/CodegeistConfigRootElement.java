package ai.codegeist.app.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import lombok.Getter;

public abstract class CodegeistConfigRootElement<T extends CodegeistConfigElement> {

    private final String rootName;

    @Getter
    @Valid
    private final T config;

    protected CodegeistConfigRootElement(String rootName) {
        this(rootName, null);
    }

    protected CodegeistConfigRootElement(String rootName, T config) {
        this.rootName = rootName;
        this.config = config;
    }

    @JsonIgnore
    public String rootName() {
        return rootName;
    }
}
