package ai.codegeist.tests.mcp;

import org.springframework.stereotype.Component;

/** Deterministic MCP tools exposed by the remote smoke fixture. */
@Component
class RemoteMcpSmokeTools {

    String remoteEcho(String text) {
        return "remote: " + text;
    }
}
