package com.example.system.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Redacts the JSON tree only; never changes controller arguments or responses. */
@Component
@RequiredArgsConstructor
public class AuditSanitizer {
    private final ObjectMapper objectMapper;

    public String toJson(Object value) throws com.fasterxml.jackson.core.JsonProcessingException {
        JsonNode tree = objectMapper.valueToTree(value);
        redact(tree);
        return objectMapper.writeValueAsString(tree);
    }

    public String sanitizeStoredJson(String payload) {
        if (payload == null || payload.isBlank()) return payload;
        try {
            JsonNode tree = objectMapper.readTree(payload);
            redact(tree);
            return objectMapper.writeValueAsString(tree);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            // Historical payloads may have been truncated in the middle of a password value.
            return "[REDACTED: invalid legacy audit payload]";
        }
    }

    private void redact(JsonNode node) {
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            object.properties().forEach(entry -> {
                String key = entry.getKey().replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
                if (key.contains("password") || key.contains("passwd") || key.contains("pwd")
                        || key.contains("secret") || key.contains("token")
                        || key.equals("authorization") || key.equals("cookie") || key.equals("apikey")) {
                    object.put(entry.getKey(), "[REDACTED]");
                } else {
                    redact(entry.getValue());
                }
            });
        } else if (node.isArray()) {
            node.forEach(this::redact);
        }
    }
}
