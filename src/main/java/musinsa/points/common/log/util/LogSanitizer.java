package musinsa.points.common.log.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class LogSanitizer {
    private static final Set<String> SENSITIVE_KEYS = new HashSet<>(Arrays.asList(
            "password","pwd","pass","authorization","apiKey","api_key","access_token","refresh_token","ssn","cardNumber"
    ));
    private static final int MAX_BODY_LENGTH = 4000;
    private static final ObjectMapper M = new ObjectMapper();


    public static String maskIfJson(String body) {
        if (body == null || body.isBlank()) return body;
        body = trimBody(body);
        try {
            JsonNode root = M.readTree(body);
            JsonNode masked = maskNode(root);
            return M.writeValueAsString(masked);
        } catch (Exception ignore) {
            return body; // not JSON
        }
    }


    public static String trimBody(String body) {
        if (body == null) return null;
        if (body.length() > MAX_BODY_LENGTH) {
            return body.substring(0, MAX_BODY_LENGTH) + "...<truncated>";
        }
        return body;
    }


    private static JsonNode maskNode(JsonNode node) {
        if (node.isObject()) {
            Iterator<String> it = node.fieldNames();
            while (it.hasNext()) {
                String name = it.next();
                JsonNode child = node.get(name);
                if (SENSITIVE_KEYS.contains(name.toLowerCase())) {
                    ((com.fasterxml.jackson.databind.node.ObjectNode) node).put(name, "***");
                } else {
                    ((com.fasterxml.jackson.databind.node.ObjectNode) node).set(name, maskNode(child));
                }
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                ((com.fasterxml.jackson.databind.node.ArrayNode) node).set(i, maskNode(node.get(i)));
            }
        }
        return node;
    }
}
