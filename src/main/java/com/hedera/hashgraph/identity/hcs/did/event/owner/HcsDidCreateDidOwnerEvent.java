package com.hedera.hashgraph.identity.hcs.did.event.owner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.PublicKey;

import java.util.HashMap;

public class HcsDidCreateDidOwnerEvent extends HcsDidEvent {

    public static String KEY_TYPE = "Ed25519VerificationKey2018";

    public HcsDidEventTargetName targetName = HcsDidEventTargetName.DID_OWNER;

        HcsDidCreateDidOwnerEvent(String id, String controller, PublicKey publicKey) throws DidError {
        super();

        if (id.isEmpty() || controller.isEmpty() || publicKey == null) {
            throw new DidError("Validation failed. DID Owner args are missing");
        }

        if (!this.isOwnerEventIdValid(id)) {
            throw new DidError("Event ID is invalid. Expected format: {did}#did-root-key");
        }

        this.type = KEY_TYPE;
        this.id = id;
        this.controller = controller;
        this.publicKey = publicKey;
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    protected String toJsonTree() {
        return null;
    }

    public String getType() {
        return type;
    }

    public String getController() {
        return controller;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
