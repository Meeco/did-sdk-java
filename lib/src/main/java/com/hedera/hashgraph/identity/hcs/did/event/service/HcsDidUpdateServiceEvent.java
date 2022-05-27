package com.hedera.hashgraph.identity.hcs.did.event.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hedera.hashgraph.identity.DidError;

public class HcsDidUpdateServiceEvent extends HcsDidCreateServiceEvent {

    public HcsDidUpdateServiceEvent(String id, ServiceType type, String serviceEndpoint) throws DidError {
        super(id, type, serviceEndpoint);
    }

    public static HcsDidCreateServiceEvent fromJsonTree(JsonNode tree) throws DidError {
        return new HcsDidUpdateServiceEvent(tree.get("id").textValue(), ServiceType.get(tree.get("type").textValue()), tree.get("serviceEndpoint").textValue());
    }

}
