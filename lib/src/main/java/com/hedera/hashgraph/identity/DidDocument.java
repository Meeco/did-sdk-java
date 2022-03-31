package com.hedera.hashgraph.identity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hedera.hashgraph.identity.hcs.did.HcsDidMessage;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;
import com.hedera.hashgraph.identity.hcs.did.event.owner.HcsDidCreateDidOwnerEvent;
import com.hedera.hashgraph.identity.hcs.did.event.owner.HcsDidUpdateDidOwnerEvent;
import com.hedera.hashgraph.identity.hcs.did.event.service.HcsDidCreateServiceEvent;
import com.hedera.hashgraph.identity.hcs.did.event.service.HcsDidUpdateServiceEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.HcsDidCreateVerificationMethodEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.HcsDidUpdateVerificationMethodEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.HcsDidCreateVerificationRelationshipEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.HcsDidRevokeVerificationRelationshipEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.HcsDidUpdateVerificationRelationshipEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.VerificationRelationshipType;
import org.threeten.bp.Instant;

import java.util.*;
import java.util.stream.Collectors;

import static com.hedera.hashgraph.identity.DidMethodOperation.CREATE;
import static com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName.DID_OWNER;


public class DidDocument {
    private final String id;
    private final String context;
    private final Map<String, JsonNode> services = new LinkedHashMap<>();
    private final Map<String, JsonNode> verificationMethods = new LinkedHashMap<>();
    private final Map<String, List<String>> verificationRelationships = new LinkedHashMap<>() {{
        put(VerificationRelationshipType.AUTHENTICATION.toString(), new ArrayList<>());
        put(VerificationRelationshipType.ASSERTION_METHOD.toString(), new ArrayList<>());
        put(VerificationRelationshipType.KEY_AGREEMENT.toString(), new ArrayList<>());
        put(VerificationRelationshipType.CAPABILITY_INVOCATION.toString(), new ArrayList<>());
        put(VerificationRelationshipType.CAPABILITY_DELEGATION.toString(), new ArrayList<>());

    }};
    private Instant created = null;
    private Instant updated = null;
    private String versionId = null;
    private boolean deactivated = false;
    private JsonNode controller;

    public DidDocument(String did, HcsDidMessage[] messages) {
        this.id = did;
        this.context = DidSyntax.DID_DOCUMENT_CONTEXT;

        this.processMessages(messages);
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
        if (!this.verificationRelationships.get(DidDocumentJsonProperties.ASSERTION_METHOD).isEmpty())
            this.verificationRelationships.get(DidDocumentJsonProperties.ASSERTION_METHOD).forEach(assertionMethodArray::add);


        ArrayNode authenticationArray = rootObject.putArray(DidDocumentJsonProperties.AUTHENTICATION);
        if (!this.verificationRelationships.get(DidDocumentJsonProperties.AUTHENTICATION).isEmpty())
            this.verificationRelationships.get(DidDocumentJsonProperties.AUTHENTICATION).forEach(authenticationArray::add);


        if (this.controller != null && !this.controller.isEmpty()) {

            ((ArrayNode) rootObject.get(DidDocumentJsonProperties.VERIFICATION_METHOD)).insert(0, this.controller);
            ((ArrayNode) rootObject.get(DidDocumentJsonProperties.ASSERTION_METHOD)).insert(0, this.controller.get("id"));
            ((ArrayNode) rootObject.get(DidDocumentJsonProperties.AUTHENTICATION)).insert(0, this.controller.get("id"));

        }

        if (!this.verificationRelationships.get(DidDocumentJsonProperties.KEY_AGREEMENT).isEmpty()) {
            ArrayNode keyAgreementArray = rootObject.putArray(DidDocumentJsonProperties.KEY_AGREEMENT);
            this.verificationRelationships.get(DidDocumentJsonProperties.KEY_AGREEMENT).forEach(keyAgreementArray::add);

        }

        if (!this.verificationRelationships.get(DidDocumentJsonProperties.CAPABILITY_INVOCATION).isEmpty()) {
            ArrayNode capabilityInvocationArray = rootObject.putArray(DidDocumentJsonProperties.CAPABILITY_INVOCATION);
            this.verificationRelationships.get(DidDocumentJsonProperties.CAPABILITY_INVOCATION).forEach(capabilityInvocationArray::add);
        }

        if (!this.verificationRelationships.get(DidDocumentJsonProperties.CAPABILITY_DELEGATION).isEmpty()) {
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
                continue;
            }

            switch (msg.getOperation()) {
                case CREATE:
                    this.processCreateMessage(msg);
                    break;
                case UPDATE:
                    this.processUpdateMessage(msg);
                    break;
                case REVOKE:
                    this.processRevokeMessage(msg);
                    break;
                case DELETE:
                    this.processDeleteMessage(msg);
                    break;
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
                    break;
                }

                this.controller = ((HcsDidCreateDidOwnerEvent) event).getOwnerDef();
                this.setDocumentActivated(message);
                break;
            case SERVICE:
                if (this.services.containsKey(event.getId())) {
                    System.out.println("Duplicate create Service event ID: " + event.getId() + ". Event will be ignored...");
                    break;
                }
                this.services.put(event.getId(), ((HcsDidCreateServiceEvent) event).getServiceDef());
                this.setDocumentUpdated(message);
                break;
            case VERIFICATION_METHOD:
                if (this.verificationMethods.containsKey(event.getId())) {
                    System.out.println("Duplicate create VerificationMethod event ID: " + event.getId() + ". Event will be ignored...");
                    break;
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
                        break;
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

    private void processUpdateMessage(HcsDidMessage message) {
        HcsDidEvent event = message.getEvent();

        switch (event.getTargetName()) {
            case DID_OWNER:
                this.controller = ((HcsDidUpdateDidOwnerEvent) event).getOwnerDef();
                this.setDocumentActivated(message);
                break;
            case SERVICE:
                if (!this.services.containsKey(event.getId())) {
                    System.out.println("Update Service event: service with ID " + event.getId() + " was not found in the document. Event will be ignored...");
                    break;
                }
                this.services.put(event.getId(), ((HcsDidUpdateServiceEvent) event).getServiceDef());
                this.setDocumentUpdated(message);
                break;
            case VERIFICATION_METHOD:
                if (!this.verificationMethods.containsKey(event.getId())) {
                    System.out.println("Update VerificationMethod event: verificationMethod with ID: " + event.getId() + " was not found in the document. Event will be ignored...");
                    break;
                }

                this.verificationMethods.put(
                        event.getId(),
                        ((HcsDidUpdateVerificationMethodEvent) event).getVerificationMethodDef()
                );
                this.setDocumentUpdated(message);
                break;
            case VERIFICATION_RELATIONSHIP:
                VerificationRelationshipType type = ((HcsDidUpdateVerificationRelationshipEvent) event).getRelationshipType();

                if (this.verificationRelationships.containsKey(type.toString())) {
                    if (!this.verificationRelationships.get(type.toString()).contains(event.getId())) {
                        System.out.println("Update VerificationRelationship event: verificationRelationship with ID: " + event.getId() + ". was not found in the document.  Event will be ignored...");
                        break;
                    }

                    this.verificationMethods.put(
                            event.getId(),
                            ((HcsDidCreateVerificationRelationshipEvent) event).getVerificationMethodDef()
                    );
                    this.setDocumentUpdated(message);
                } else {
                    System.out.println("Update verificationRelationship event with type" + type + "is not supported. Event will be ignored...");
                }
                break;
            default:
                System.out.println("Update" + event.getTargetName() + " operation is not supported. Event will be ignored...");
        }
    }

    private void processRevokeMessage(HcsDidMessage message) {
        HcsDidEvent event = message.getEvent();

        switch (event.getTargetName()) {
            case SERVICE:
                if (!this.services.containsKey(event.getId())) {
                    System.out.println("Revoke Service event: service with ID " + event.getId() + " was not found in the document. Event will be ignored...");
                    break;
                }
                this.services.remove(event.getId());
                this.setDocumentUpdated(message);
                break;
            case VERIFICATION_METHOD:
                if (!this.verificationMethods.containsKey(event.getId())) {
                    System.out.println("Revoke VerificationMethod event: verificationMethod with ID: " + event.getId() + " was not found in the document. Event will be ignored...");
                    break;
                }

                this.verificationMethods.remove(event.getId());
                this.verificationRelationships.keySet().forEach(key -> this.verificationRelationships.put(key, this.verificationRelationships.get(key).stream().filter(id -> !Objects.equals(id, event.getId())).collect(Collectors.toList())));

                this.setDocumentUpdated(message);
                break;
            case VERIFICATION_RELATIONSHIP:
                VerificationRelationshipType type = ((HcsDidRevokeVerificationRelationshipEvent) event).getRelationshipType();

                if (this.verificationRelationships.containsKey(type.toString())) {
                    if (!this.verificationRelationships.get(type.toString()).contains(event.getId())) {
                        System.out.println("Revoke VerificationRelationship event: verificationRelationship with ID: " + event.getId() + ". was not found in the document.  Event will be ignored...");
                        break;
                    }

                    this.verificationRelationships.put(type.toString(), this.verificationRelationships.get(type.toString()).stream().filter(id -> !Objects.equals(id, event.getId())).collect(Collectors.toList()));

                    boolean canRemoveVerificationMethod = this.verificationRelationships.values().stream().noneMatch(v -> v.contains(event.getId()));
                    if (canRemoveVerificationMethod) {
                        this.verificationMethods.remove(event.getId());
                    }

                    this.setDocumentUpdated(message);
                } else {
                    System.out.println("Revoke verificationRelationship event with type" + type + "is not supported. Event will be ignored...");
                }
                break;
            default:
                System.out.println("Revoke" + event.getTargetName() + " operation is not supported. Event will be ignored...");
        }
    }

    private void processDeleteMessage(HcsDidMessage message) {
        HcsDidEvent event = message.getEvent();

        if (event.getTargetName() == HcsDidEventTargetName.Document) {
            this.controller = null;
            this.services.clear();
            this.verificationMethods.clear();
            this.verificationRelationships.keySet().forEach(
                    key -> this.verificationRelationships.put(key, new ArrayList<>())
            );
            this.setDocumentDeactivated();
        } else {
            System.out.println("Delete" + event.getTargetName() + " operation is not supported. Event will be ignored...");
        }

    }
}
