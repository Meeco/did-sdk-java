package com.hedera.hashgraph.identity.hcs.did;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.DidErrorCode;
import com.hedera.hashgraph.identity.DidSyntax;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TopicId;
import org.javatuples.Triplet;

import java.util.Objects;

/**
 * Hedera Decentralized Identifier for Hedera DID Method specification based on HCS.
 */
public class HcsDid {

    public static String DID_METHOD = DidSyntax.METHOD_HEDERA_HCS;

    protected Client client;
    protected PrivateKey privateKey;
    protected String identifier;
    protected String network;
    protected TopicId topicId;


    public HcsDid(
            Optional<String> identifier,
            Optional<PrivateKey> privateKey,
            Optional<Client> client
    ) throws DidError {
        this.identifier = identifier.isPresent() ? identifier.get() : null;
        this.privateKey = privateKey.isPresent() ? privateKey.get() : null;
        this.client = client.isPresent() ? client.get() : null;


        if (!identifier.isPresent() && !privateKey.isPresent()) {
            throw new DidError("identifier and privateKey cannot both be empty");
        }

        if (identifier.isPresent()) {
            Triplet<String, TopicId, String> parseIdentifier = HcsDid.parseIdentifier(this.identifier);
            this.network = parseIdentifier.getValue0();
            this.topicId = parseIdentifier.getValue1();
        }
    }

    public static Triplet<String, TopicId, String> parseIdentifier(String identifier) throws DidError {
        String[] array = identifier.split(DidSyntax.DID_TOPIC_SEPARATOR);

        if (array.length != 2)
            throw new DidError("DID string is invalid: topic ID is missing", DidErrorCode.INVALID_DID_STRING);

        String topicIdPart = array[1];
        if (Strings.isNullOrEmpty(topicIdPart)) {
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


                Triplet<String, TopicId, String> result =
                        new Triplet<>(networkName, topicId, didIdString);
                return result;

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

    public TopicId getTopicId() {
        return this.topicId;
    }


}
