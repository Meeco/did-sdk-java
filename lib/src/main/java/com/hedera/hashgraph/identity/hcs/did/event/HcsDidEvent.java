package com.hedera.hashgraph.identity.hcs.did.event;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.HcsDid;
import com.hedera.hashgraph.identity.utils.Hashing;

import java.util.regex.Pattern;

public abstract class HcsDidEvent {
    protected final Pattern SERVICE_ID_POSTFIX_REGEX = Pattern.compile("^(service)-[0-9]+$");
    protected final Pattern KEY_ID_POSTFIX_REGEX = Pattern.compile("^(key)-[0-9]+$");
    protected final Pattern OWNER_KEY_POSTFIX_REGEX = Pattern.compile("^(did-root-key)$");
    protected HcsDidEventTargetName targetName;


    protected HcsDidEvent(HcsDidEventTargetName targetName) {
        this.targetName = targetName;
    }

    protected abstract String getId();


    protected abstract JsonNode toJsonTree();


    protected abstract String toJSON();

    protected String getBase64() {
        return Hashing.Base64.encode(this.toJSON());
    }


    protected boolean isOwnerEventIdValid(String eventId) throws DidError {

        return this.isEventIdValid(eventId, this.OWNER_KEY_POSTFIX_REGEX);

    }

    protected boolean isServiceEventIdValid(String eventId) throws DidError {
        return this.isEventIdValid(eventId, this.SERVICE_ID_POSTFIX_REGEX);

    }

    protected boolean isKeyEventIdValid(String eventId) throws DidError {
        return this.isEventIdValid(eventId, this.KEY_ID_POSTFIX_REGEX);
    }

    protected boolean isEventIdValid(String eventId, Pattern pattern) throws DidError {
        if (Strings.isNullOrEmpty(eventId))
            return false;


        String[] eventIdentifiers = eventId.split("#");

        if (eventIdentifiers.length < 2)
            return false;

        String identifier = eventIdentifiers[0];

        String id = eventIdentifiers[1];

        if (Strings.isNullOrEmpty(identifier) || Strings.isNullOrEmpty(id)) {
            return false;
        }

        HcsDid.parseIdentifier(identifier);

        return pattern.matcher(id).find();
    }


}
