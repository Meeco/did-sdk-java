package com.hedera.hashgraph.identity.hcs.did.event.owner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.PublicKey;

import java.util.LinkedHashMap;
import java.util.Map;

public class HcsDidCreateDidOwnerEvent extends HcsDidEvent {

    private static final String KEY_TYPE = "Ed25519VerificationKey2018";

    protected String id;
    protected String type = KEY_TYPE;
    protected String controller;
    protected PublicKey publicKey;

    public HcsDidCreateDidOwnerEvent(String id, String controller, PublicKey publicKey) throws DidError {
        super(HcsDidEventTargetName.DID_OWNER);

        if (Strings.isNullOrEmpty(id) || Strings.isNullOrEmpty(controller) || publicKey == null) {
            throw new DidError("Validation failed. DID Owner args are missing");
        }

        if (!this.isOwnerEventIdValid(id)) {
            throw new DidError("Event ID is invalid. Expected format: {did}#did-root-key");
        }

        this.id = id;
        this.controller = controller;
        this.publicKey = publicKey;
    }

    public static HcsDidCreateDidOwnerEvent fromJsonTree(JsonNode tree) throws DidError {
        PublicKey publicKey = PublicKey.fromBytes(Hashing.Multibase.decode(tree.get("publicKeyMultibase").textValue()));
        return new HcsDidCreateDidOwnerEvent(tree.get("id").textValue(), tree.get("controller").textValue(), publicKey);
    }

    @Override
    public String getId() {
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

    public String getPublicKeyMultibase() {
        return Hashing.Multibase.encode(this.getPublicKey().toBytes());
    }

    public JsonNode getOwnerDef() {
        Map<String, Object> ownerDefMap = getOwnerDefMap();

        return new ObjectMapper().valueToTree(ownerDefMap);
    }

    @Override
    public JsonNode toJsonTree() {

        Map<String, Object> ownerDefMap = getOwnerDefMap();

        Map<String, Map<String, Object>> owner = new LinkedHashMap<>();
        owner.put(this.targetName.toString(), ownerDefMap);

        return new ObjectMapper().valueToTree(owner);
    }

    @Override
    protected String toJSON() {
        return this.toJsonTree().toString();
    }

    private Map<String, Object> getOwnerDefMap() {
        Map<String, Object> ownerDefMap = new LinkedHashMap<>();
        ownerDefMap.put("id", this.getId());
        ownerDefMap.put("type", this.getType());
        ownerDefMap.put("controller", this.getController());
        ownerDefMap.put("publicKeyMultibase", this.getPublicKeyMultibase());
        return ownerDefMap;
    }
}
