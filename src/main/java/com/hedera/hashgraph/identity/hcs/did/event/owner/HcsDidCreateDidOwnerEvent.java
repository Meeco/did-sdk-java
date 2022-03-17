package com.hedera.hashgraph.identity.hcs.did.event.owner;

import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;
import com.hedera.hashgraph.sdk.PublicKey;

public class HcsDidCreateDidOwnerEvent extends HcsDidEvent {

    public static String KEY_TYPE = "Ed25519VerificationKey2018";

    public HcsDidEventTargetName targetName = HcsDidEventTargetName.DID_OWNER;

    String id;
    String type = HcsDidCreateDidOwnerEvent.KEY_TYPE;
    String controller;
    PublicKey publicKey;

    HcsDidCreateDidOwnerEvent(String id, String controller, PublicKey publicKey) throws DidError {
        super();

        if (id.isEmpty() || controller.isEmpty() || publicKey == null) {
            throw new DidError("Validation failed. DID Owner args are missing");
        }

        if (!this.isOwnerEventIdValid(id)) {
            throw new DidError("Event ID is invalid. Expected format: {did}#did-root-key");
        }

        this.id = id;
        this.controller = controller;
        this.publicKey = publicKey;
    }
    @Override
    protected String getId() {
        return id;
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

    @Override
    protected String toJsonTree() {
        return type;
    }

    @Override
    protected String toJSON() {
        return null;
    }
}
