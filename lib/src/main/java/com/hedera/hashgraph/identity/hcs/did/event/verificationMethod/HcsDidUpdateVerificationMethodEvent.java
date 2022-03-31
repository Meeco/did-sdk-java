package com.hedera.hashgraph.identity.hcs.did.event.verificationMethod;

import com.fasterxml.jackson.databind.JsonNode;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.PublicKey;

public class HcsDidUpdateVerificationMethodEvent extends HcsDidCreateVerificationMethodEvent {

    public HcsDidUpdateVerificationMethodEvent(String id, VerificationMethodSupportedKeyType type, String controller, PublicKey publicKey) throws DidError {
        super(id, type, controller, publicKey);
    }

    public static HcsDidUpdateVerificationMethodEvent fromJsonTree(JsonNode tree) throws DidError {
        PublicKey publicKey = PublicKey.fromBytes(Hashing.Multibase.decode(tree.get("publicKeyMultibase").textValue()));
        return new HcsDidUpdateVerificationMethodEvent(tree.get("id").textValue(), VerificationMethodSupportedKeyType.get(tree.get("type").textValue()), tree.get("controller").textValue(), publicKey);
    }
}
