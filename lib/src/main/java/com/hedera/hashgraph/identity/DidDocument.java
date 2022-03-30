package com.hedera.hashgraph.identity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hedera.hashgraph.identity.hcs.did.HcsDidMessage;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.owner.HcsDidCreateDidOwnerEvent;
import com.hedera.hashgraph.identity.hcs.did.event.service.HcsDidCreateServiceEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.HcsDidCreateVerificationMethodEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.HcsDidCreateVerificationRelationshipEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.VerificationRelationshipType;
import org.threeten.bp.Instant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.hedera.hashgraph.identity.DidMethodOperation.CREATE;
import static com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName.DID_OWNER;


public class DidDocument {


    private final String id;
    private final String context;
    private final Map<String, JsonNode> services = new LinkedHashMap<>();
    private final Map<String, JsonNode> verificationMethods = new LinkedHashMap<>();
    private final Map<String, List<String>> verificationRelationships = new LinkedHashMap<>();
    private Instant created = null;
    private Instant updated = null;
    private String versionId = null;
    private boolean deactivated = false;
    private JsonNode controller;

    public DidDocument(String did, HcsDidMessage[] messages) {
        this.id = did;
        this.context = DidSyntax.DID_DOCUMENT_CONTEXT;

        // this.processMessages(messages);
    }


    public boolean getDeactivated() {
        return this.deactivated;
    }

    public String getVersionId() {
        return this.versionId;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public Instant getCreated() {
        return this.created;
    }

    public boolean hasOwner() {
        return !(this.controller == null || this.controller.isEmpty());
    }

    public String getContext() {
        return this.context;
    }

    public String getId() {
        return this.id;
    }


    public JsonNode toJsonTree() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode rootObject = (ObjectNode) objectMapper.readTree("{}");

        rootObject.put(DidDocumentJsonProperties.CONTEXT, this.context);
        rootObject.put(DidDocumentJsonProperties.ID, this.id);

        if (this.controller != null && !Objects.equals(this.id, this.controller.get("controller").textValue())) {
            rootObject.set(DidDocumentJsonProperties.CONTROLLER, this.controller.get("controller"));
        }

        rootObject.putArray(DidDocumentJsonProperties.VERIFICATION_METHOD).addAll(this.verificationMethods.values());

        ArrayNode assertionMethodArray = rootObject.putArray(DidDocumentJsonProperties.ASSERTION_METHOD);
        this.verificationRelationships.get(DidDocumentJsonProperties.ASSERTION_METHOD).forEach(assertionMethodArray::add);


        ArrayNode authenticationArray = rootObject.putArray(DidDocumentJsonProperties.AUTHENTICATION);
        this.verificationRelationships.get(DidDocumentJsonProperties.AUTHENTICATION).forEach(authenticationArray::add);


        if (this.controller != null && !this.controller.isEmpty()) {

            ((ArrayNode) rootObject.get(DidDocumentJsonProperties.VERIFICATION_METHOD)).insert(0, this.controller.get("controller"));
            ((ArrayNode) rootObject.get(DidDocumentJsonProperties.ASSERTION_METHOD)).insert(0, this.controller.get("controller").get("id"));
            ((ArrayNode) rootObject.get(DidDocumentJsonProperties.AUTHENTICATION)).insert(0, this.controller.get("controller").get("id"));

        }

        if (this.verificationRelationships.get(DidDocumentJsonProperties.KEY_AGREEMENT) != null && this.verificationRelationships.get(DidDocumentJsonProperties.KEY_AGREEMENT).isEmpty()) {
            ArrayNode keyAgreementArray = rootObject.putArray(DidDocumentJsonProperties.KEY_AGREEMENT);
            this.verificationRelationships.get(DidDocumentJsonProperties.KEY_AGREEMENT).forEach(keyAgreementArray::add);

        }

        if (this.verificationRelationships.get(DidDocumentJsonProperties.CAPABILITY_INVOCATION) != null && this.verificationRelationships.get(DidDocumentJsonProperties.CAPABILITY_INVOCATION).isEmpty()) {
            ArrayNode capabilityInvocationArray = rootObject.putArray(DidDocumentJsonProperties.CAPABILITY_INVOCATION);
            this.verificationRelationships.get(DidDocumentJsonProperties.CAPABILITY_INVOCATION).forEach(capabilityInvocationArray::add);
        }

        if (this.verificationRelationships.get(DidDocumentJsonProperties.CAPABILITY_DELEGATION) != null && this.verificationRelationships.get(DidDocumentJsonProperties.CAPABILITY_DELEGATION).isEmpty()) {
            ArrayNode capabilityDelegationArray = rootObject.putArray(DidDocumentJsonProperties.CAPABILITY_DELEGATION);
            this.verificationRelationships.get(DidDocumentJsonProperties.CAPABILITY_DELEGATION).forEach(capabilityDelegationArray::add);

        }

        if (!this.services.isEmpty()) {
            rootObject.putArray(DidDocumentJsonProperties.SERVICE).addAll(this.services.values());
        }

        return rootObject;
    }

    public String toJSON() throws JsonProcessingException {
        return this.toJsonTree().toString();
    }

    private void setDocumentActivated(HcsDidMessage message) {
        Instant timestamp = message.getTimestamp();

        this.created = timestamp;
        this.updated = timestamp;
        this.deactivated = false;
        this.versionId = timestamp.toString();
    }

    private void setDocumentDeactivated() {
        this.created = null;
        this.updated = null;
        this.deactivated = true;
        this.versionId = null;
    }


    private void setDocumentUpdated(HcsDidMessage message) {
        Instant timestamp = message.getTimestamp();

        this.updated = timestamp;
        this.versionId = timestamp.toString();
    }


    private void processMessages(HcsDidMessage[] messages) {

        for (HcsDidMessage msg : messages) {
            if (
                    this.controller == null &&
                            msg.getOperation() == CREATE &&
                            msg.getEvent().getTargetName() != DID_OWNER
            ) {
                System.out.println("DID document owner is not registered. Event will be ignored...");
                return;
            }

            switch (msg.getOperation()) {
                case CREATE:
                    this.processCreateMessage(msg);
                    break;
//                case DidMethodOperation.UPDATE:
//                    this.processUpdateMessage(msg);
//                    break;
//                case DidMethodOperation.REVOKE:
//                    this.processRevokeMessage(msg);
//                    break;
//                case DidMethodOperation.DELETE:
//                    this.processDeleteMessage(msg);
//                    break;
                default:
                    System.out.println("Operation " + msg.getOperation() + "is not supported. Event will be ignored...");
            }
        }

    }

    private void processCreateMessage(HcsDidMessage message) {
        HcsDidEvent event = message.getEvent();

        switch (event.getTargetName()) {
            case DID_OWNER:
                if (this.controller != null && !this.controller.isEmpty()) {
                    System.out.println("DID owner is already registered: " + this.controller + ". Event will be ignored...");
                    return;
                }

                this.controller = ((HcsDidCreateDidOwnerEvent) event).getOwnerDef();
                this.setDocumentActivated(message);
                break;
            case SERVICE:
                if (this.services.containsKey(event.getId())) {
                    System.out.println("Duplicate create Service event ID: " + event.getId() + ". Event will be ignored...");
                    return;
                }
                this.services.put(event.getId(), ((HcsDidCreateServiceEvent) event).getServiceDef());
                this.setDocumentUpdated(message);
                return;
            case VERIFICATION_METHOD:
                if (this.verificationMethods.containsKey(event.getId())) {
                    System.out.println("Duplicate create VerificationMethod event ID: " + event.getId() + ". Event will be ignored...");
                    return;
                }

                this.verificationMethods.put(
                        event.getId(),
                        ((HcsDidCreateVerificationMethodEvent) event).getVerificationMethodDef()
                );
                this.setDocumentUpdated(message);
                break;
            case VERIFICATION_RELATIONSHIP:
                VerificationRelationshipType type = ((HcsDidCreateVerificationRelationshipEvent) event).getRelationshipType();

                if (this.verificationRelationships.containsKey(type.toString())) {
                    if (this.verificationRelationships.get(type.toString()).contains(event.getId())) {
                        System.out.println("Duplicate create VerificationRelationship event ID: " + event.getId() + ". Event will be ignored...");
                        return;
                    }

                    this.verificationRelationships.get(type.toString()).add(event.getId());

                    if (!this.verificationMethods.containsKey(event.getId())) {
                        this.verificationMethods.put(
                                event.getId(),
                                ((HcsDidCreateVerificationRelationshipEvent) event).getVerificationMethodDef()
                        );
                    }
                    this.setDocumentUpdated(message);
                } else {
                    System.out.println("Create verificationRelationship event with type" + type + "is not supported. Event will be ignored...");
                }
                break;
            default:
                System.out.println("Create" + event.getTargetName() + " operation is not supported. Event will be ignored...");
        }
    }

}
