package com.hedera.hashgraph.identity.hcs.did.event.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;

public class HcsDidDeleteEvent extends HcsDidEvent {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public String toJsonTree() {
        return null;
    }

    @Override
    public String toJSON() {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(this.toJsonTree());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonNode == null ? EMPTY_STRING : jsonNode.asText();
    }

    static HcsDidDeleteEvent fromJsonTree(Object tree) {
        return new HcsDidDeleteEvent();
    }
}
