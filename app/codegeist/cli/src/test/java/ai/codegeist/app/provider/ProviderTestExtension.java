package ai.codegeist.app.provider;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

final class ProviderTestExtension implements ExecutionCondition {

    static final String CATEGORY_ENV = "CODEGEIST_TEST_PROVIDER_CATEGORY";

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        ProviderCategory providerCategory = context.getElement()
                .map(element -> element.getAnnotation(ProviderCategory.class))
                .orElse(null);
        if (providerCategory == null) {
            return ConditionEvaluationResult.enabled("No provider category required");
        }

        ProviderTestCategory activeCategory = ProviderTestCategory.valueOf(
                System.getenv().getOrDefault(CATEGORY_ENV, ProviderTestCategory.none.name()));
        ProviderTestCategory requiredCategory = providerCategory.value();
        if (activeCategory.allows(requiredCategory)) {
            return ConditionEvaluationResult.enabled(
                    "Provider category " + activeCategory.name() + " allows " + requiredCategory.name());
        }

        return ConditionEvaluationResult.disabled(
                "Provider category " + requiredCategory.name() + " requires " + CATEGORY_ENV + "="
                        + requiredCategory.name() + " or higher");
    }
}
