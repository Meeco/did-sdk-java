package com.hedera.hashgraph.identity.hcs.did.event.verificationMethod;

import java.util.Arrays;

public enum VerificationMethodSupportedKeyType {
    ED25519_VERIFICATION_KEY_2018("Ed25519VerificationKey2018");

    private final String type;

    VerificationMethodSupportedKeyType(final String typeName) {
        this.type = typeName;
    }

    public static VerificationMethodSupportedKeyType get(final String typeName) {
        return Arrays.stream(values())
                .filter(t -> t.type.equals(typeName)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid DID verification method supported type name: " + typeName));
    }

    @Override
    public String toString() {
        return type;
    }
}