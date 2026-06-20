package ai.codegeist.app.tool;

import org.springframework.util.StringUtils;

/**
 * Raw JSON payload for one Codegeist-owned local tool invocation.
 *
 * <p>Spring AI still calls local tools with a string payload. Wrapping that string at
 * the Codegeist boundary gives local tools a domain type instead of passing raw JSON
 * through every execute contract.
 */
record CodegeistToolInput(String json) {

    static final String EMPTY_JSON = "{}";

    CodegeistToolInput {
        json = StringUtils.hasText(json) ? json : EMPTY_JSON;
    }
}
