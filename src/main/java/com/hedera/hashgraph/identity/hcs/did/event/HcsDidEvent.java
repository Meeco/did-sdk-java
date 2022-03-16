package com.hedera.hashgraph.identity.hcs.did.event;

import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.HcsDid;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

public abstract class HcsDidEvent {
    protected static final Pattern SERVICE_ID_POSTFIX_REGEX = Pattern.compile("/^(service)\\-[0-9]+$/");
    protected static final Pattern KEY_ID_POSTFIX_REGEX = Pattern.compile("/^(key)\\-[0-9]+$/");
    protected static final Pattern OWNER_KEY_POSTFIX_REGEX = Pattern.compile("/^(did-root\\-key)$/");

    protected static final String EMPTY_STRING = "";
    protected static final String HASH_SIGN_STRING = "#";

    HcsDidEventTargetName targetName;
    String id;

    abstract String getId();

    abstract String toJsonTree();

    abstract String toJSON();

    public String getBase64() {
        return new String(Base64.getEncoder().encode(this.toJSON().getBytes()), StandardCharsets.UTF_8);
    }

    protected Boolean isOwnerEventIdValid(String eventId) {

        return isEventIdValid(eventId, this.OWNER_KEY_POSTFIX_REGEX);


    }

    protected Boolean isServiceEventIdValid(String eventId) {
        return isEventIdValid(eventId, this.SERVICE_ID_POSTFIX_REGEX);


    }

    protected Boolean isKeyEventIdValid(String eventId) {
        return isEventIdValid(eventId, this.KEY_ID_POSTFIX_REGEX);


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
}
