package com.hedera.hashgraph.identity.hcs.did.event.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;

import java.util.HashMap;
import java.util.Map;

public class HcsDidCreateServiceEvent extends HcsDidEvent {

    protected String id;
    protected ServiceType type;
    protected String serviceEndpoint;


    protected HcsDidCreateServiceEvent(String id, ServiceType type, String serviceEndpoint) throws DidError {
        super(HcsDidEventTargetName.SERVICE);

        if (Strings.isNullOrEmpty(id) || type == null || Strings.isNullOrEmpty(type.toString()) || Strings.isNullOrEmpty(serviceEndpoint)) {
            throw new DidError("Validation failed. Services args are missing");
        }

        if (!this.isServiceEventIdValid(id)) {
            throw new DidError("Event ID is invalid. Expected format: {did}#service-{integer}");
        }

        this.id = id;
        this.type = type;
        this.serviceEndpoint = serviceEndpoint;

    }

    public static HcsDidCreateServiceEvent fromJsonTree(JsonNode tree) throws DidError {
        return new HcsDidCreateServiceEvent(tree.get("id").textValue(), ServiceType.get(tree.get("type").textValue()), tree.get("serviceEndpoint").textValue());
    }

    public ServiceType getType() {
        return this.type;
    }

    public String getServiceEndpoint() {
        return this.serviceEndpoint;
    }

    public JsonNode getServiceDef() {

        Map<String, Object> serviceDefMap = getServiceDefMap();

        return new ObjectMapper().valueToTree(serviceDefMap);

    }

    @Override
    protected String getId() {
        return this.id;
    }

    @Override
    protected JsonNode toJsonTree() {

        Map<String, Object> serviceDefMap = getServiceDefMap();

        Map<String, Map<String, Object>> service = new HashMap<>();
        service.put(this.targetName.toString(), serviceDefMap);

        return new ObjectMapper().valueToTree(service);
    }

    @Override
    protected String toJSON() {
        return this.toJsonTree().toString();
    }

    private Map<String, Object> getServiceDefMap() {
        Map<String, Object> serviceDefMap = new HashMap<>();
        serviceDefMap.put("id", this.getId());
        serviceDefMap.put("type", this.getType().toString());
        serviceDefMap.put("serviceEndpoint", this.getServiceEndpoint());
        return serviceDefMap;
    }

}
