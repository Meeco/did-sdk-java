package com.hedera.hashgraph.identity;

public enum DidMethodOperation {

    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    REVOKE("revoke");

    public final String label;

    DidMethodOperation(String label) {

        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
