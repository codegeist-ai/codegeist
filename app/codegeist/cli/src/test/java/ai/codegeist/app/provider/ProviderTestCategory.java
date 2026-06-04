package ai.codegeist.app.provider;

enum ProviderTestCategory {

    none,
    local,
    remote_free,
    remote_paid;

    boolean allows(ProviderTestCategory requiredCategory) {
        return ordinal() >= requiredCategory.ordinal();
    }
}
