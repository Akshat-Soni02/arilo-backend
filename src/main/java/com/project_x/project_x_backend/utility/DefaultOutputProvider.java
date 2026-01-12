package com.project_x.project_x_backend.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class DefaultOutputProvider {
    private final ObjectMapper mapper;

    public DefaultOutputProvider(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonNode getFallbackStt() {
        ObjectNode node = mapper.createObjectNode();
        node.put("stt", "Something went wrong, please record again");
        node.put("language", "unknown");
        node.put("tasks", mapper.createArrayNode());
        node.put("anxiety_score", 0);
        node.put("tags", mapper.createArrayNode());
        return node;
    }

    public JsonNode getFallbackNoteback() {
        ObjectNode node = mapper.createObjectNode();
        node.put("noteback", "");
        return node;
    }
}