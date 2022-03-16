package com.hedera.hashgraph.identity.hcs.did.event.service;

import java.util.Arrays;

public enum ServiceType {

    LINKED_DOMAINS("LinkedDomains"),
    DID_COMM_MESSAGING("DIDCommMessaging");

    private String type;

    ServiceType(final String typeName) {
        this.type = typeName;
    }

    public static ServiceType get(final String typeName) {
        return Arrays.stream(values())
                .filter(t -> t.type.equals(typeName)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid DID service type name: " + typeName));
    }

    @Override
    public String toString() {
        return type;
    }
}
