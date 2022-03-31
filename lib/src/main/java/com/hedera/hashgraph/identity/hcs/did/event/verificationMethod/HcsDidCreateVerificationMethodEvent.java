package com.hedera.hashgraph.identity.hcs.did.event.verificationMethod;

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

public class HcsDidCreateVerificationMethodEvent extends HcsDidEvent {


    protected String id;
    protected VerificationMethodSupportedKeyType type;
    protected String controller;
    protected PublicKey publicKey;

    public HcsDidCreateVerificationMethodEvent(String id, VerificationMethodSupportedKeyType type, String controller, PublicKey publicKey) throws DidError {
        super(HcsDidEventTargetName.VERIFICATION_METHOD);

        if (Strings.isNullOrEmpty(id) || type == null || Strings.isNullOrEmpty(type.toString()) || Strings.isNullOrEmpty(controller) || publicKey == null) {
            throw new DidError("Validation failed. Verification Method args are missing");
        }

        if (!this.isKeyEventIdValid(id)) {
            throw new DidError("Event ID is invalid. Expected format: {did}#key-{integer}");
        }

        this.id = id;
        this.type = type;
        this.controller = controller;
        this.publicKey = publicKey;
    }

    public static HcsDidCreateVerificationMethodEvent fromJsonTree(JsonNode tree) throws DidError {
        PublicKey publicKey = PublicKey.fromBytes(Hashing.Multibase.decode(tree.get("publicKeyMultibase").textValue()));
        return new HcsDidCreateVerificationMethodEvent(tree.get("id").textValue(), VerificationMethodSupportedKeyType.get(tree.get("type").textValue()), tree.get("controller").textValue(), publicKey);
    }

    public String getPublicKeyMultibase() {
        return Hashing.Multibase.encode(this.getPublicKey().toBytes());
    }

    @Override
    public String getId() {
        return this.id;
    }

    public VerificationMethodSupportedKeyType getType() {
        return this.type;
    }

    public String getController() {
        return this.controller;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
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
        verificationMethodDef.put("type", this.getType().toString());
        verificationMethodDef.put("controller", this.getController());
        verificationMethodDef.put("publicKeyMultibase", this.getPublicKeyMultibase());
        return verificationMethodDef;
    }
}
