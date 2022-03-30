package com.hedera.hashgraph.identity;

import com.hedera.hashgraph.identity.hcs.did.HcsDidMessage;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class DidDocument {

    private final Instant created = null;
    private final Instant updated = null;
    private final String versionId = null;
    private final boolean deactivated = false;
    private final Map<String, Object> services = new LinkedHashMap<>();
    private final Map<String, Object> verificationMethods = new LinkedHashMap<>();
    private final Map<String, String[]> verificationRelationships = new LinkedHashMap<>();
    private final String id;
    private final String context;
    private Map<String, Object> controller;

    DidDocument(String did, HcsDidMessage[] messages) {
        this.id = did;
        this.context = DidSyntax.DID_DOCUMENT_CONTEXT;

        this.processMessages(messages);
    }

    private void processMessages(HcsDidMessage[] messages) {

//        for (HcsDidMessage msg : messages) {
//            if (
//                    !this.controller &&
//                            msg.getOperation() === DidMethodOperation.CREATE &&
//                            msg.getEvent().targetName !== HcsDidEventTargetName.DID_OWNER
//            ) {
//                console.warn("DID document owner is not registered. Event will be ignored...");
//                return;
//            }
//
//            switch (msg.getOperation()) {
//                case DidMethodOperation.CREATE:
//                    this.processCreateMessage(msg);
//                    return;
//                case DidMethodOperation.UPDATE:
//                    this.processUpdateMessage(msg);
//                    return;
//                case DidMethodOperation.REVOKE:
//                    this.processRevokeMessage(msg);
//                    return;
//                case DidMethodOperation.DELETE:
//                    this.processDeleteMessage(msg);
//                    return;
//                default:
//                    console.warn(`Operation ${msg.getOperation()} is not supported. Event will be ignored...`);
//            }
//        }

    }

}
