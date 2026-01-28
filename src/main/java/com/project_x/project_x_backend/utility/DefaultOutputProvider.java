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
        ObjectNode sttResponse = mapper.createObjectNode();
        sttResponse.put("stt", "Something went wrong, please record again");
        sttResponse.put("language", "unknown");
        sttResponse.set("tasks", mapper.createArrayNode());
        sttResponse.put("anxiety_score", 0);
        sttResponse.set("tags", mapper.createArrayNode());
        node.set("stt_response", sttResponse);
        node.set("sentences_with_embeddings", mapper.createArrayNode());
        return node;
    }

    public JsonNode getFallbackNoteback() {
        ObjectNode node = mapper.createObjectNode();
        ObjectNode notebackResponse = mapper.createObjectNode();
        notebackResponse.put("noteback", "Something went wrong, please record again");
        node.set("noteback_response", notebackResponse);
        node.set("sentences_with_embeddings", mapper.createArrayNode());
        return node;
    }
}