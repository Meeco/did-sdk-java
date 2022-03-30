package com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship;

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

public class HcsDidCreateVerificationRelationshipEvent extends HcsDidEvent {

    protected String id;
    protected VerificationRelationshipSupportedKeyType type;
    protected VerificationRelationshipType relationshipType;
    protected String controller;
    protected PublicKey publicKey;

    protected HcsDidCreateVerificationRelationshipEvent(
            String id,
            VerificationRelationshipType relationshipType,
            VerificationRelationshipSupportedKeyType type,
            String controller,
            PublicKey publicKey

    ) throws DidError {
        super(HcsDidEventTargetName.VERIFICATION_RELATIONSHIP);

        if (Strings.isNullOrEmpty(id) || type == null || Strings.isNullOrEmpty(type.toString()) || relationshipType == null
                || Strings.isNullOrEmpty(relationshipType.toString()) || Strings.isNullOrEmpty(controller) || publicKey == null) {
            throw new DidError("Validation failed. Verification Relationship args are missing");
        }

        if (!this.isKeyEventIdValid(id)) {
            throw new DidError("Event ID is invalid. Expected format: {did}#key-{integer}");
        }

        this.id = id;
        this.type = type;
        this.relationshipType = relationshipType;
        this.controller = controller;
        this.publicKey = publicKey;

    }

    public static HcsDidCreateVerificationRelationshipEvent fromJsonTree(JsonNode tree) throws DidError {
        PublicKey publicKey = PublicKey.fromBytes(Hashing.Multibase.decode(tree.get("publicKeyMultibase").textValue()));
        return new HcsDidCreateVerificationRelationshipEvent(
                tree.get("id").textValue(),
                VerificationRelationshipType.get(tree.get("relationshipType").textValue()),
                VerificationRelationshipSupportedKeyType.get(tree.get("type").textValue()),
                tree.get("controller").textValue(),
                publicKey);
    }

    @Override
    public String getId() {
        return this.id;
    }

    public VerificationRelationshipSupportedKeyType getType() {
        return this.type;
    }

    public VerificationRelationshipType getRelationshipType() {
        return this.relationshipType;
    }

    public String getController() {
        return this.controller;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public String getPublicKeyMultibase() {
        return Hashing.Multibase.encode(this.getPublicKey().toBytes());
    }

    public JsonNode getVerificationMethodDef() {

        Map<String, Object> verificationMethodDef = getVerificationMethodDefMap();
        return new ObjectMapper().valueToTree(verificationMethodDef);

    }

    @Override
    protected JsonNode toJsonTree() {

        Map<String, Object> verificationMethodDef = getVerificationMethodDefMap();

        Map<String, Map<String, Object>> verificationMethod = new LinkedHashMap<>();
        verificationMethod.put(this.targetName.toString(), verificationMethodDef);
        return new ObjectMapper().valueToTree(verificationMethod);
    }

    @Override
    protected String toJSON() {
        return this.toJsonTree().toString();
    }

    private Map<String, Object> getVerificationMethodDefMap() {
        Map<String, Object> verificationMethodDef = new LinkedHashMap<>();
        verificationMethodDef.put("id", this.getId());
        verificationMethodDef.put("relationshipType", this.getRelationshipType().toString());
        verificationMethodDef.put("type", this.getType().toString());
        verificationMethodDef.put("controller", this.getController());
        verificationMethodDef.put("publicKeyMultibase", this.getPublicKeyMultibase());
        return verificationMethodDef;
    }
}
