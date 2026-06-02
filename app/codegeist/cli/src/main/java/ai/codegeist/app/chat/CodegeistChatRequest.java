package ai.codegeist.app.chat;

import ai.codegeist.app.config.ProviderConfig;

public record CodegeistChatRequest(ProviderConfig providerConfig, String prompt) {
}
