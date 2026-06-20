package ai.codegeist.app.tool;

import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * Package-local contract for one Spring-discovered Codegeist local tool.
 *
 * <p>Implementations are Spring components injected into {@link CodegeistLocalTools}
 * as a list. A local tool may work with files, shell commands, git state, test
 * runners, or another local runtime capability. Adding a new local tool should only
 * require a new implementation plus tests; the callback assembler does not need to
 * know the concrete tool name or domain.
 */
interface CodegeistLocalTool {

    ToolDefinition definition();

    CodegeistToolResult execute(CodegeistToolInput toolInput);
}
