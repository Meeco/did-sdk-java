package com.hedera.hashgraph.identity.hcs.did.event;

public enum HcsDidEventTargetName {
    DID_OWNER("DIDOwner"),
    VERIFICATION_METHOD("VerificationMethod"),
    VERIFICATION_RELATIONSHIP("VerificationRelationship"),
    SERVICE("Service"),
    Document("Document");

    public final String label;

    HcsDidEventTargetName(String label) {

        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
