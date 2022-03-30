package com.hedera.hashgraph.identity.hcs.did.event.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;

import java.util.LinkedHashMap;
import java.util.Map;

public class HcsDidRevokeServiceEvent extends HcsDidEvent {

    protected String id;

    protected HcsDidRevokeServiceEvent(String id) throws DidError {
        super(HcsDidEventTargetName.SERVICE);

        if (Strings.isNullOrEmpty(id)) {
            throw new DidError("Validation failed. Services args are missing");
        }

        if (!super.isServiceEventIdValid(id)) {
            throw new DidError("Event ID is invalid. Expected format: {did}#service-{integer}");
        }

        this.id = id;
    }

    public static HcsDidRevokeServiceEvent fromJsonTree(JsonNode tree) throws DidError {
        return new HcsDidRevokeServiceEvent(tree.get("id").textValue());
    }

    @Override
    protected String getId() {
        return this.id;
    }

    @Override
    protected JsonNode toJsonTree() {
        Map<String, Object> serviceDefMap = new LinkedHashMap<>();
        serviceDefMap.put("id", this.getId());


        Map<String, Map<String, Object>> service = new LinkedHashMap<>();
        service.put(this.targetName.toString(), serviceDefMap);

        return new ObjectMapper().valueToTree(service);
    }

    @Override
    protected String toJSON() {
        return this.toJsonTree().toString();
    }
}
