package com.hedera.hashgraph.identity;

import java.util.Arrays;

public enum DidMethodOperation {

    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    REVOKE("revoke");

    public final String label;

    DidMethodOperation(String label) {

        this.label = label;
    }

    public static DidMethodOperation get(final String typeName) {
        return Arrays.stream(values())
                .filter(t -> t.label.equals(typeName)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid DID service type name: " + typeName));
    }

    @Override
    public String toString() {
        return label;
    }
}
