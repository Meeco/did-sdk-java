package com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship;

import java.util.Arrays;

public enum VerificationRelationshipType {

    AUTHENTICATION("authentication"),
    ASSERTION_METHOD("assertionMethod"),
    KEY_AGREEMENT("keyAgreement"),
    CAPABILITY_INVOCATION("capabilityInvocation"),
    CAPABILITY_DELEGATION("capabilityDelegation");

    private String type;

    VerificationRelationshipType(final String typeName) {
        this.type = typeName;
    }

    public static VerificationRelationshipType get(final String typeName) {
        return Arrays.stream(values())
                .filter(k -> k.type.equals(typeName)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid DID verification relationship type name: " + typeName));
    }

    @Override
    public String toString() {
        return type;
    }
}
