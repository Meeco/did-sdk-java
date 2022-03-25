package com.hedera.hashgraph.identity.hcs.did.event.verificationMethod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;

import java.util.HashMap;
import java.util.Map;

public class HcsDidRevokeVerificationMethodEvent extends HcsDidEvent {

    protected String id;

    protected HcsDidRevokeVerificationMethodEvent(String id) throws DidError {
        super(HcsDidEventTargetName.VERIFICATION_METHOD);


        if (Strings.isNullOrEmpty(id)) {
            throw new DidError("Validation failed. Services args are missing");
        }

        if (!this.isKeyEventIdValid(id)) {
            throw new DidError("Event ID is invalid. Expected format: {did}#service-{integer}");
        }

        this.id = id;
    }

    public static HcsDidRevokeVerificationMethodEvent fromJsonTree(JsonNode tree) throws DidError {

        return new HcsDidRevokeVerificationMethodEvent(tree.get("id").textValue());
    }

    @Override
    protected String getId() {
        return this.id;
    }

    @Override
    protected JsonNode toJsonTree() {
        Map<String, Object> verificationMethodDefMap = new HashMap<>();
        verificationMethodDefMap.put("id", this.getId());


        Map<String, Map<String, Object>> service = new HashMap<>();
        service.put(this.targetName.toString(), verificationMethodDefMap);

        return new ObjectMapper().valueToTree(service);
    }

    @Override
    protected String toJSON() {
        return this.toJsonTree().toString();
    }
}
