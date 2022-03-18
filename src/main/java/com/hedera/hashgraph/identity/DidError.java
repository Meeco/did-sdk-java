package com.hedera.hashgraph.identity;

public class DidError extends Exception {


    public DidErrorCode code = DidErrorCode.GENERIC;

    public DidError(String message) {
        super(message);
    }
    public DidError(String message, DidErrorCode errorCode) {
        super(message);
        this.code = errorCode;
    }
}
