package com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship;

import com.fasterxml.jackson.databind.JsonNode;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.PublicKey;

public class HcsDidUpdateVerificationRelationshipEvent extends HcsDidCreateVerificationRelationshipEvent {

    protected HcsDidUpdateVerificationRelationshipEvent(String id, VerificationRelationshipType relationshipType, VerificationRelationshipSupportedKeyType type, String controller, PublicKey publicKey) throws DidError {
        super(id, relationshipType, type, controller, publicKey);
    }

    public static HcsDidUpdateVerificationRelationshipEvent fromJsonTree(JsonNode tree) throws DidError {
        PublicKey publicKey = PublicKey.fromBytes(Hashing.Multibase.decode(tree.get("publicKeyMultibase").textValue()));
        return new HcsDidUpdateVerificationRelationshipEvent(
                tree.get("id").textValue(),
                VerificationRelationshipType.get(tree.get("relationshipType").textValue()),
                VerificationRelationshipSupportedKeyType.get(tree.get("type").textValue()),
                tree.get("controller").textValue(),
                publicKey);
    }
}
