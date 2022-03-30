package com.hedera.hashgraph.identity.hcs.did.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.identity.DidMethodOperation;
import com.hedera.hashgraph.identity.hcs.did.event.document.HcsDidDeleteEvent;
import com.hedera.hashgraph.identity.hcs.did.event.owner.HcsDidCreateDidOwnerEvent;
import com.hedera.hashgraph.identity.hcs.did.event.owner.HcsDidUpdateDidOwnerEvent;
import com.hedera.hashgraph.identity.hcs.did.event.service.HcsDidCreateServiceEvent;
import com.hedera.hashgraph.identity.hcs.did.event.service.HcsDidRevokeServiceEvent;
import com.hedera.hashgraph.identity.hcs.did.event.service.HcsDidUpdateServiceEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.HcsDidCreateVerificationMethodEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.HcsDidRevokeVerificationMethodEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.HcsDidUpdateVerificationMethodEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.HcsDidCreateVerificationRelationshipEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.HcsDidRevokeVerificationRelationshipEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.HcsDidUpdateVerificationRelationshipEvent;
import com.hedera.hashgraph.identity.utils.Hashing;

import java.lang.reflect.Method;
import java.util.Map;

import static java.util.Map.entry;

public class HcsDidEventParser {

    private static final Map<DidMethodOperation, Map<HcsDidEventTargetName, Class<? extends HcsDidEvent>>> EVENT_NAME_TO_CLASS =
            Map.ofEntries(
                    entry(DidMethodOperation.CREATE, Map.ofEntries(
                            entry(HcsDidEventTargetName.DID_OWNER, HcsDidCreateDidOwnerEvent.class),
                            entry(HcsDidEventTargetName.SERVICE, HcsDidCreateServiceEvent.class),
                            entry(HcsDidEventTargetName.VERIFICATION_METHOD, HcsDidCreateVerificationMethodEvent.class),
                            entry(HcsDidEventTargetName.VERIFICATION_RELATIONSHIP, HcsDidCreateVerificationRelationshipEvent.class))),

                    entry(DidMethodOperation.UPDATE, Map.ofEntries(
                            entry(HcsDidEventTargetName.DID_OWNER, HcsDidUpdateDidOwnerEvent.class),
                            entry(HcsDidEventTargetName.SERVICE, HcsDidUpdateServiceEvent.class),
                            entry(HcsDidEventTargetName.VERIFICATION_METHOD, HcsDidUpdateVerificationMethodEvent.class),
                            entry(HcsDidEventTargetName.VERIFICATION_RELATIONSHIP, HcsDidUpdateVerificationRelationshipEvent.class))),

                    entry(DidMethodOperation.REVOKE, Map.ofEntries(
                            entry(HcsDidEventTargetName.SERVICE, HcsDidRevokeServiceEvent.class),
                            entry(HcsDidEventTargetName.VERIFICATION_METHOD, HcsDidRevokeVerificationMethodEvent.class),
                            entry(HcsDidEventTargetName.VERIFICATION_RELATIONSHIP, HcsDidRevokeVerificationRelationshipEvent.class)))


            );


    public static HcsDidEvent fromBase64(DidMethodOperation operation, String eventBase64) {


        if (operation == DidMethodOperation.DELETE) {
            return HcsDidDeleteEvent.fromJsonTree(null);
        }

        try {

            JsonNode tree = new ObjectMapper().readTree(Hashing.Base64.decode(eventBase64));

            Map<HcsDidEventTargetName, Class<? extends HcsDidEvent>> eventsByOperation = EVENT_NAME_TO_CLASS.get(operation);
            JsonNode finalTree = tree;
            Map.Entry<HcsDidEventTargetName, Class<? extends HcsDidEvent>> eventTarget = eventsByOperation.entrySet().stream().filter(es -> es.getKey().toString() == finalTree.fields().next().getKey()).findFirst().get();

            Method fromJsonTree = eventTarget.getValue().getMethod("fromJsonTree", JsonNode.class);
            HcsDidEvent invoke = (HcsDidEvent) fromJsonTree.invoke(null, tree.get(eventTarget.getKey().toString()));
            return invoke;

        } catch (Exception e) {
            return null;
        }

    }

}
