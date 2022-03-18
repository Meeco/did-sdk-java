package com.hedera.hashgraph.identity.hcs.did.event.owner;

public class OwnerDef {
    String id;
    String type;
    String controller;
    String publicKeyMultibase;

    public String getId() {
        return id;
    }

    public OwnerDef setId(String id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public OwnerDef setType(String type) {
        this.type = type;
        return this;
    }

    public String getController() {
        return controller;
    }

    public OwnerDef setController(String controller) {
        this.controller = controller;
        return this;
    }

    public String getPublicKeyMultibase() {
        return publicKeyMultibase;
    }

    public OwnerDef setPublicKeyMultibase(String publicKeyMultibase) {
        this.publicKeyMultibase = publicKeyMultibase;
        return this;
    }
}
