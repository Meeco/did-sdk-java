package com.hedera.hashgraph.identity.hcs.did.event.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;

public class HcsDidDeleteEvent extends HcsDidEvent {

    protected HcsDidDeleteEvent() {
        super(HcsDidEventTargetName.Document);
    }

    public String getBase64() {
        return null;
    }

    @Override
    protected String getId() {
        return null;
    }

    @Override
    protected JsonNode toJsonTree() {
        return null;
    }

    @Override
    protected String toJSON() {
        return "null";
    }
}
