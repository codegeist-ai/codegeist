package ai.codegeist.app.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodegeistYamlExpressionEvaluator {

    private static final String EXPRESSION_MARKER = "#{";
    static final String SPEL_EVALUATION_ERROR_PREFIX = "Failed to evaluate SpEL expression at ";
    private static final TemplateParserContext TEMPLATE_PARSER_CONTEXT = new TemplateParserContext(
            EXPRESSION_MARKER, "}");

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    private final CodegeistConfigYamlMapper yamlMapper;

    public JsonNode evaluate(JsonNode root, String sourcePath) {
        return evaluateNode(root, sourcePath, "");
    }

    private JsonNode evaluateNode(JsonNode node, String sourcePath, String yamlPath) {
        if (node == null || node.isNull()) {
            return JsonNodeFactory.instance.nullNode();
        }

        if (node instanceof ObjectNode objectNode) {
            ObjectNode evaluated = JsonNodeFactory.instance.objectNode();
            for (Map.Entry<String, JsonNode> field : objectNode.properties()) {
                evaluated.set(field.getKey(), evaluateNode(field.getValue(), sourcePath,
                        childPath(yamlPath, field.getKey())));
            }
            return evaluated;
        }

        if (node instanceof ArrayNode arrayNode) {
            ArrayNode evaluated = JsonNodeFactory.instance.arrayNode();
            for (int index = 0; index < arrayNode.size(); index++) {
                evaluated.add(evaluateNode(arrayNode.get(index), sourcePath, yamlPath + "[" + index + "]"));
            }
            return evaluated;
        }

        if (node.isTextual() && node.asText().contains(EXPRESSION_MARKER)) {
            return evaluateScalar(node.asText(), sourcePath, yamlPath);
        }

        return node.deepCopy();
    }

    private JsonNode evaluateScalar(String value, String sourcePath, String yamlPath) {
        try {
            if (isWholeExpression(value)) {
                Object result = parser.parseExpression(value.substring(2, value.length() - 1))
                        .getValue(evaluationContext);
                return yamlMapper.valueToTree(result);
            }

            String result = parser.parseExpression(value, TEMPLATE_PARSER_CONTEXT)
                    .getValue(evaluationContext, String.class);
            return result == null ? JsonNodeFactory.instance.nullNode() : TextNode.valueOf(result);
        } catch (RuntimeException ex) {
            throw new CodegeistConfigValidationException(SPEL_EVALUATION_ERROR_PREFIX + sourcePath + ":" + yamlPath,
                    ex);
        }
    }

    private boolean isWholeExpression(String value) {
        return value.startsWith(EXPRESSION_MARKER) && value.endsWith("}") && value.indexOf(EXPRESSION_MARKER, 2) < 0;
    }

    private String childPath(String parent, String child) {
        return parent.isEmpty() ? child : parent + "." + child;
    }
}
