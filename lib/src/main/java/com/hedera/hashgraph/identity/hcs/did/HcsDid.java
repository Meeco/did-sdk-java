package com.hedera.hashgraph.identity.hcs.did;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.hedera.hashgraph.identity.*;
import com.hedera.hashgraph.identity.hcs.MessageEnvelope;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.owner.HcsDidCreateDidOwnerEvent;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.*;
import org.awaitility.Awaitility;
import org.javatuples.Triplet;

import java.security.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Hedera Decentralized Identifier for Hedera DID Method specification based on HCS.
 */
public class HcsDid {

    protected static final Duration MIRROR_NODE_TIMEOUT = Duration.ofSeconds(30);
    public static String DID_METHOD = DidSyntax.METHOD_HEDERA_HCS;
    public static Integer READ_TOPIC_MESSAGES_TIMEOUT = 5000;
    public static Hbar TRANSACTION_FEE = new Hbar(2);

    protected Client client;
    protected PrivateKey privateKey;
    protected String identifier;
    protected String network;
    protected TopicId topicId;

    protected HcsDidMessage[] messages;
    protected Timestamp resolvedAt;
    protected DidDocument document;


    public HcsDid(
            String identifier,
            PrivateKey privateKey,
            Client client
    ) throws DidError {
        this.identifier = identifier;
        this.privateKey = privateKey;
        this.client = client;


        if (this.identifier == null && privateKey == null) {
            throw new DidError("identifier and privateKey cannot both be empty");
        }

        if (identifier != null) {
            Triplet<String, TopicId, String> parseIdentifier = HcsDid.parseIdentifier(this.identifier);
            this.network = parseIdentifier.getValue0();
            this.topicId = parseIdentifier.getValue1();
        }
    }

    public static Triplet<String, TopicId, String> parseIdentifier(String identifier) throws DidError {
        String[] array = identifier.split(DidSyntax.DID_TOPIC_SEPARATOR);

        if (array.length != 2) {
            throw new DidError("DID string is invalid: topic ID is missing", DidErrorCode.INVALID_DID_STRING);
        }

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


                return new Triplet<>(networkName, topicId, didIdString);

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

    public static String publicKeyToIdString(PublicKey publicKey) {
        return Hashing.Multibase.encode(publicKey.toBytes());
    }

    public static PublicKey stringToPublicKey(String idString) {
        return PublicKey.fromBytes(Hashing.Multibase.decode(idString));
    }

    public DidDocument resolve() throws DidError {
        if (this.identifier == null) {
            throw new DidError("DID is not registered");
        }

        if (this.client == null) {
            throw new DidError("Client configuration is missing");
        }

        AtomicReference<List<MessageEnvelope<HcsDidMessage>>> messageRef = new AtomicReference<>(null);


        new HcsDidEventMessageResolver(this.topicId)
                .setTimeout(HcsDid.READ_TOPIC_MESSAGES_TIMEOUT)
                .whenFinished(messageRef::set)
                .execute(this.client);


        // Wait until mirror node resolves the DID.
        Awaitility.await().atMost(MIRROR_NODE_TIMEOUT).until(() -> messageRef.get() != null);

        this.messages = messageRef.get().stream().map(MessageEnvelope::open).collect(Collectors.toList()).toArray(HcsDidMessage[]::new);
        this.document = new DidDocument(this.identifier, this.messages);

        return this.document;
    }

    public HcsDid register() throws DidError, TimeoutException, PrecheckStatusException, ReceiptStatusException, JsonProcessingException {
        this.validateClientConfig();

        if (this.identifier != null) {
            this.resolve();

            if (this.document.hasOwner()) {
                throw new DidError("DID is already registered");
            }
        } else {
            TopicCreateTransaction topicCreateTransaction = new TopicCreateTransaction()
                    .setMaxTransactionFee(HcsDid.TRANSACTION_FEE)
                    .setAdminKey(this.privateKey)
                    .setSubmitKey(this.privateKey.getPublicKey())
                    .freezeWith(this.client);

            TopicCreateTransaction sigTx = topicCreateTransaction.sign(this.privateKey);
            TransactionResponse txResponse = sigTx.execute(this.client);
            TransactionRecord txRecord = txResponse.getRecord(this.client);

            this.topicId = txRecord.receipt.topicId;
            this.network = Objects.requireNonNull(this.client.getLedgerId()).toString();
            this.identifier = this.buildIdentifier(this.privateKey.getPublicKey());
        }

        HcsDidCreateDidOwnerEvent event = new HcsDidCreateDidOwnerEvent(
                this.identifier + "#did-root-key",
                this.identifier,
                this.privateKey.getPublicKey()
        );

        this.submitTransaction(DidMethodOperation.CREATE, event, this.privateKey);

        return this;
    }

    public TopicId getTopicId() {
        return this.topicId;
    }

    private void validateClientConfig() throws DidError {
        if (this.privateKey == null) {
            throw new DidError("privateKey is missing");
        }

        if (this.client == null) {
            throw new DidError("Client configuration is missing");
        }
    }

    private String buildIdentifier(PublicKey publicKey) {
        String methodNetwork = String.join(DidSyntax.DID_METHOD_SEPARATOR, HcsDid.DID_METHOD, this.network);

        return DidSyntax.DID_PREFIX +
                DidSyntax.DID_METHOD_SEPARATOR +
                methodNetwork +
                DidSyntax.DID_METHOD_SEPARATOR +
                HcsDid.publicKeyToIdString(publicKey) +
                DidSyntax.DID_TOPIC_SEPARATOR +
                this.topicId.toString();

    }


    private MessageEnvelope<HcsDidMessage> submitTransaction(DidMethodOperation didMethodOperation, HcsDidEvent event, PrivateKey privateKey) throws DidError, JsonProcessingException {
        HcsDidMessage message = new HcsDidMessage(didMethodOperation, this.identifier, event);
        MessageEnvelope envelope = new MessageEnvelope(message);
        HcsDidTransaction transaction = new HcsDidTransaction(envelope, this.topicId);

        AtomicReference<MessageEnvelope<HcsDidMessage>> messageRef = new AtomicReference<>(null);

        transaction
                .signMessage(privateKey::sign)
                .buildAndSignTransaction(tx -> tx.setMaxTransactionFee(HcsDid.TRANSACTION_FEE).freezeWith(this.client).sign(this.privateKey))
                .onMessageConfirmed(messageRef::set).execute(this.client);

        // Wait until mirror node resolves the DID.
        Awaitility.await().atMost(MIRROR_NODE_TIMEOUT).until(() -> messageRef.get() != null);

        return messageRef.get();
    }

    /**
     * Attribute getters
     */

    public String getIdentifier() {
        return this.identifier;
    }

    public Client getClient() {
        return this.client;
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }


    public String getNetwork() {
        return this.network;
    }

    public String getMethod() {
        return HcsDid.DID_METHOD;
    }

    public HcsDidMessage[] getMessages() {
        return this.messages;
    }


}
