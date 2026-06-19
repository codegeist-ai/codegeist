package ai.codegeist.app.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
public abstract class CodegeistTypedConfigElement extends CodegeistConfigElement {

    @Getter
    @Setter
    @NotBlank
    private String type;
}
