package com.hedera.hashgraph.identity;

import com.hedera.hashgraph.identity.hcs.did.HcsDid;

/**
 * DID parser for Hedera DIDs.
 */
public final class DidParser {

    /**
     * Private default constructor.
     */
    private DidParser() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    /**
     * Parses the given DID string into it's corresponding Hedera DID object.
     *
     * @param didString DID string.
     * @return {@link HcsDid} instance.
     */
    public static HcsDid parse(final String didString) throws DidError {
        final int methodIndex = DidSyntax.DID_PREFIX.length() + 1;
        if (didString == null || didString.length() <= methodIndex) {
            throw new IllegalArgumentException("DID string cannot be null");
        }

        if (didString.startsWith(HcsDid.DID_METHOD + DidSyntax.DID_METHOD_SEPARATOR, methodIndex)) {
            return new HcsDid(didString, null, null);
        } else {
            throw new IllegalArgumentException("DID string is invalid.");
        }
    }
}
