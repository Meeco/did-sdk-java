package com.hedera.hashgraph.identity.hcs.did.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.HcsDid;
import com.hedera.hashgraph.identity.hcs.did.event.owner.OwnerDef;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.PublicKey;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

public abstract class HcsDidEvent {
    protected static final Pattern SERVICE_ID_POSTFIX_REGEX = Pattern.compile("/^(service)-[0-9]+$/");
    protected static final Pattern KEY_ID_POSTFIX_REGEX = Pattern.compile("/^(key)-[0-9]+$/");
    protected static final Pattern OWNER_KEY_POSTFIX_REGEX = Pattern.compile("/^(did-root-key)$/");

    protected static final String EMPTY_STRING = "";
    protected static final String HASH_SIGN_STRING = "#";

    HcsDidEventTargetName targetName;
    protected String id;
    protected String type;
    protected String controller;
    protected PublicKey publicKey;
    protected static ObjectMapper objectMapper = new ObjectMapper();

    protected abstract String toJsonTree();

    protected String toJSON(){
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(this.toJsonTree());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonNode == null ? EMPTY_STRING : jsonNode.asText();
    }

    public String getBase64() {
        return new String(Base64.getEncoder().encode(this.toJSON().getBytes()), StandardCharsets.UTF_8);
    }

    static HcsDidEvent fromJSONTree(Object tree){
        throw new Error("not implemented");
    }

    protected Boolean isOwnerEventIdValid(String eventId) {

        return isEventIdValid(eventId, OWNER_KEY_POSTFIX_REGEX);


    }

    protected Boolean isServiceEventIdValid(String eventId) {
        return isEventIdValid(eventId, SERVICE_ID_POSTFIX_REGEX);


    }

    protected Boolean isKeyEventIdValid(String eventId) {
        return isEventIdValid(eventId, KEY_ID_POSTFIX_REGEX);


    }

    private Boolean isEventIdValid(String eventId, Pattern pattern) {
        String identifier = EMPTY_STRING;
        String id = EMPTY_STRING;

        if (!eventId.isEmpty()) {
            String[] ids = eventId.split(HASH_SIGN_STRING);
            if (ids.length == 2) {
                id = ids[0];
                identifier = ids[1];
            } else {
                return false;
            }
        }

        if (identifier.isEmpty() || id.isEmpty()) {
            return false;
        }

        try {
            HcsDid.parseIdentifier(identifier);
        } catch (DidError e) {
            e.printStackTrace();
        }

        return pattern.matcher(id).find();
    }
    public OwnerDef getOwnerDef(HcsDidEvent event) {
        return new OwnerDef()
                .setId(event.getId())
                .setType(event.getType())
                .setController(event.getController())
                .setPublicKeyMultibase(event.getPublicKeyMultibase());
    }
    public String getPublicKeyMultibase() {
        return Hashing.MultibaseClass.encode(this.getPublicKey().toBytes());
    }

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
}
