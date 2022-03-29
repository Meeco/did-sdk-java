package com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;

import java.util.LinkedHashMap;
import java.util.Map;

public class HcsDidRevokeVerificationRelationshipEvent extends HcsDidEvent {


    protected String id;
    protected VerificationRelationshipType relationshipType;

    protected HcsDidRevokeVerificationRelationshipEvent(String id, VerificationRelationshipType relationshipType) throws DidError {
        super(HcsDidEventTargetName.VERIFICATION_RELATIONSHIP);

        if (Strings.isNullOrEmpty(id) || relationshipType == null || Strings.isNullOrEmpty(relationshipType.toString())) {
            throw new DidError("Validation failed. Verification Relationship args are missing");
        }

        if (!this.isKeyEventIdValid(id)) {
            throw new DidError("Event ID is invalid. Expected format: {did}#key-{integer}");
        }

        this.id = id;
        this.relationshipType = relationshipType;

    }

    public static HcsDidRevokeVerificationRelationshipEvent fromJsonTree(JsonNode tree) throws DidError {
        return new HcsDidRevokeVerificationRelationshipEvent(tree.get("id").textValue(),
                VerificationRelationshipType.get(tree.get("relationshipType").textValue()));
    }

    @Override
    protected String getId() {
        return this.id;
    }

    public VerificationRelationshipType getRelationshipType() {
        return this.relationshipType;
    }

    @Override
    protected JsonNode toJsonTree() {
        Map<String, Object> verificationMethodDef = new LinkedHashMap<>();
        verificationMethodDef.put("id", this.getId());
        verificationMethodDef.put("relationshipType", this.getRelationshipType().toString());

        Map<String, Map<String, Object>> verificationMethod = new LinkedHashMap<>();
        verificationMethod.put(this.targetName.toString(), verificationMethodDef);

        return new ObjectMapper().valueToTree(verificationMethod);
    }

    @Override
    protected String toJSON() {
        return this.toJsonTree().toString();
    }
}
