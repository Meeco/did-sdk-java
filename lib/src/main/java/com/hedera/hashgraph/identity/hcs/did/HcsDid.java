package com.hedera.hashgraph.identity.hcs.did;

import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.DidErrorCode;
import com.hedera.hashgraph.identity.DidSyntax;
import com.hedera.hashgraph.sdk.TopicId;

import java.util.Objects;

/**
 * Hedera Decentralized Identifier for Hedera DID Method specification based on HCS.
 */
public class HcsDid {


    public static void parseIdentifier(String identifier) throws DidError {
        String[] array = identifier.split(DidSyntax.DID_TOPIC_SEPARATOR);
        if (array.length == 2) {
            String topicIdPart = array[1];
            if (topicIdPart.isEmpty()) {
                throw new DidError("DID string is invalid: topic ID is missing", DidErrorCode.INVALID_DID_STRING);
            }

            TopicId topicId = TopicId.fromString(topicIdPart);

            String[] didParts = array[0].split(DidSyntax.DID_METHOD_SEPARATOR);
            if (didParts.length == 4) {
                if (!Objects.equals(didParts[0], DidSyntax.DID_PREFIX)) {
                    throw new DidError("DID string is invalid: invalid prefix.", DidErrorCode.INVALID_DID_STRING);
                }

                String methodName = didParts[1];
                if (!Objects.equals(DidSyntax.METHOD_HEDERA_HCS, methodName)) {
                    throw new DidError(
                            "DID string is invalid: invalid method name: " + methodName,
                            DidErrorCode.INVALID_DID_STRING
                    );
                }

                try {
                    String networkName = didParts[2];

                    if (
                            !Objects.equals(networkName, DidSyntax.HEDERA_NETWORK_MAINNET) &&
                                    !Objects.equals(networkName, DidSyntax.HEDERA_NETWORK_TESTNET) &&
                                    !Objects.equals(networkName, DidSyntax.HEDERA_NETWORK_PREVIEWNET)
                    ) {
                        throw new DidError("DID string is invalid. Invalid Hedera network.", DidErrorCode.INVALID_NETWORK);
                    }

                    String didIdString = didParts[3];

                    if (didIdString.length() < 48) {
                        throw new DidError(
                                "DID string is invalid. ID holds incorrect format.",
                                DidErrorCode.INVALID_DID_STRING
                        );
                    }

                    // return [networkName, topicId, didIdString];

                } catch (Exception e) {
                    if (e instanceof DidError) {
                        throw e;
                    }

                    throw new DidError("DID string is invalid. " + e.getMessage(), DidErrorCode.INVALID_DID_STRING);
                }
            } else {
                throw new DidError(
                        "DID string is invalid. ID holds incorrect format.",
                        DidErrorCode.INVALID_DID_STRING);
            }
        }
    }


}
