package com.hedera.hashgraph.identity.hcs.did.event.document;

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
        return null;
    }

    static HcsDidDeleteEvent fromJsonTree(Object tree) {
        return new HcsDidDeleteEvent();
    }
}
