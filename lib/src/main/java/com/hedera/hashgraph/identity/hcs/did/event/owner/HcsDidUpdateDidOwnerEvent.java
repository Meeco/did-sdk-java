package com.hedera.hashgraph.identity.hcs.did.event.owner;


import com.fasterxml.jackson.databind.JsonNode;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.PublicKey;

public class HcsDidUpdateDidOwnerEvent extends HcsDidCreateDidOwnerEvent {

    HcsDidUpdateDidOwnerEvent(String id, String controller, PublicKey publicKey) throws DidError {
        super(id, controller, publicKey);
    }

    public static HcsDidUpdateDidOwnerEvent fromJsonTree(JsonNode tree) throws DidError {
        PublicKey publicKey = PublicKey.fromBytes(Hashing.Multibase.decode(tree.get("publicKeyMultibase").textValue()));
        return new HcsDidUpdateDidOwnerEvent(tree.get("id").textValue(), tree.get("controller").textValue(), publicKey);
    }


}
