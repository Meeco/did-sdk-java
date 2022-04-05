package com.hedera.hashgraph.identity.hcs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.hedera.hashgraph.identity.hcs.did.HcsDidMessage;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TopicMessage;
import org.threeten.bp.Instant;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class MessageEnvelope<T extends HcsDidMessage> {

    private static final String MESSAGE_KEY = "message";
    private static final String SIGNATURE_KEY = "signature";

    protected T message;
    protected String signature;
    protected SerializableMirrorConsensusResponse mirrorResponse;

    public MessageEnvelope() {
    }

    public MessageEnvelope(T message) {
        this.message = message;
    }

    /**
     * Converts a message from a DID or VC topic response into object instance.
     *
     * @param <U>          Type of the message inside envelope.
     * @param response     Topic message as a response from mirror node.
     * @param messageClass Class type of the message inside envelope.
     * @return The {@link MessageEnvelope}.
     * @throws JsonProcessingException   if problems encountered when processing (parsing, generating) JSON content of message
     * @throws NoSuchMethodException     throws when method fromJsonTree not found in messageClass
     * @throws InvocationTargetException throws when fail to invoke fromJsonTree method of messageClass
     * @throws IllegalAccessException    throws when fail to invoke fromJsonTree method of messageClass
     */
    public static <U extends HcsDidMessage> MessageEnvelope<U> fromMirrorResponse(
            final TopicMessage response, final Class<U> messageClass) throws JsonProcessingException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String msgJson = new String(response.contents, StandardCharsets.UTF_8);

        MessageEnvelope<U> result = MessageEnvelope.fromJson(msgJson, messageClass);
        result.mirrorResponse = new SerializableMirrorConsensusResponse(response);

        return result;
    }

    /**
     * Converts a VC topic message from a JSON string into object instance.
     *
     * @param <U>          Type of the message inside envelope.
     * @param json         VC topic message as JSON string.
     * @param messageClass Class of the message inside envelope.
     * @return The {@link MessageEnvelope}.
     * @throws JsonProcessingException   if problems encountered when processing (parsing, generating) JSON content of message
     * @throws NoSuchMethodException     throws when method fromJsonTree not found in messageClass
     * @throws InvocationTargetException throws when fail to invoke fromJsonTree method of messageClass
     * @throws IllegalAccessException    throws when fail to invoke fromJsonTree method of messageClass
     */
    public static <U extends HcsDidMessage> MessageEnvelope<U> fromJson(final String json, final Class<U> messageClass) throws JsonProcessingException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        MessageEnvelope<U> result = new MessageEnvelope<>();

        // extract original message JSON part separately to be able to verify signature.
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        if (jsonNode.has(MESSAGE_KEY)) {
            Method fromJsonTree = messageClass.getMethod("fromJsonTree", JsonNode.class);
            result.message = (U) fromJsonTree.invoke(null, jsonNode.get(MESSAGE_KEY));
        } else {
            result.message = null;
        }

        if (jsonNode.has(SIGNATURE_KEY) && jsonNode.get(SIGNATURE_KEY) != null) {
            result.signature = jsonNode.get(SIGNATURE_KEY).toString();
        }

        return result;
    }

    protected String getMessageJson() throws JsonProcessingException {
        if (this.message == null) {
            return null;
        }
        return this.message.toJSON();
    }


    /**
     * Signs this message envelope with the given signing function.
     *
     * @param signer The signing function.
     * @return This envelope signed and serialized to JSON, ready for submission to HCS topic.
     * @throws JsonProcessingException if problems encountered when processing (parsing, generating) JSON content of message
     */
    public byte[] sign(final UnaryOperator<byte[]> signer) throws JsonProcessingException {
        if (signer == null) {
            throw new IllegalArgumentException("Signing function is not provided.");
        }

        if (!Strings.isNullOrEmpty(signature)) {
            throw new IllegalStateException("Message is already signed.");
        }

        byte[] msgBytes = this.message.toJSON().getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = signer.apply(msgBytes);
        signature = new String(Base64.getEncoder().encode(signatureBytes), StandardCharsets.UTF_8);

        return this.toJSON().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Signs this message envelope with the given signing function.
     *
     * @param privateKey The private key to sign with.
     * @return This envelope signed and serialized to JSON, ready for submission to HCS topic.
     * @throws JsonProcessingException if problems encountered when processing (parsing, generating) JSON content of message
     */
    public byte[] sign(final PrivateKey privateKey) throws JsonProcessingException {
        if (privateKey == null) {
            throw new IllegalArgumentException("Signing private key is not provided.");
        }

        if (!Strings.isNullOrEmpty(signature)) {
            throw new IllegalStateException("Message is already signed.");
        }

        byte[] msgBytes = this.message.toJSON().getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = privateKey.sign(msgBytes);
        signature = new String(Base64.getEncoder().encode(signatureBytes), StandardCharsets.UTF_8);

        return this.toJSON().getBytes(StandardCharsets.UTF_8);
    }

    public JsonNode toJsonTree() throws JsonProcessingException {
        JsonNode result = new ObjectMapper().readTree("{}");
        if (this.message != null) {
            ((ObjectNode) result).set(MESSAGE_KEY, this.message.toJsonTree());
        }
        if (!Strings.isNullOrEmpty(this.signature)) {
            ((ObjectNode) result).put(SIGNATURE_KEY, this.signature);
        }
        return result;
    }

    public String toJSON() throws JsonProcessingException {
        return this.toJsonTree().toString();
    }

    /**
     * Verifies the signature of the envelope against the public key of it's signer.
     *
     * @param publicKeyProvider Provider of a public key of this envelope signer.
     * @return True if the message is valid, false otherwise.
     * @throws JsonProcessingException if problems encountered when processing (parsing, generating) JSON content of message
     */
    public boolean isSignatureValid(final Function<MessageEnvelope<T>, PublicKey> publicKeyProvider) throws JsonProcessingException {
        if (Strings.isNullOrEmpty(signature) || message == null) {
            return false;
        }

        PublicKey publicKey = publicKeyProvider.apply(this);
        if (publicKey == null) {
            return false;
        }

        byte[] signatureToVerify = Base64.getDecoder().decode(signature.getBytes(StandardCharsets.UTF_8));
        byte[] messageBytes = this.message.toJSON().getBytes(StandardCharsets.UTF_8);

        return publicKey.verify(messageBytes, signatureToVerify);
    }

    public T open() {
        return this.message;
    }

    public String getSignature() {
        return this.signature;
    }

    public Instant getConsensusTimestamp() {
        return this.mirrorResponse == null ? null : this.mirrorResponse.consensusTimestamp;
    }

    public SerializableMirrorConsensusResponse getMirrorResponse() {
        return this.mirrorResponse;
    }


}
