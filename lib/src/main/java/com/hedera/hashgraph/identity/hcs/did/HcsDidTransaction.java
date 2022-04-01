package com.hedera.hashgraph.identity.hcs.did;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.MessageEnvelope;
import com.hedera.hashgraph.identity.utils.Validator;
import com.hedera.hashgraph.sdk.*;
import org.threeten.bp.Instant;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class HcsDidTransaction {

    protected TopicId topicId;
    protected MessageEnvelope<HcsDidMessage> message;
    private boolean executed;
    private Function<TopicMessageSubmitTransaction, Transaction> buildTransactionFunction;
    private Consumer<MessageEnvelope<HcsDidMessage>> receiver;
    private Consumer<Throwable> errorHandler;
    private UnaryOperator<byte[]> signer;
    private HcsDidTopicListener listener;

    /**
     * Instantiates a new transaction object from a message that was already prepared.
     *
     * @param message The message envelope.
     * @param topicId The HCS DID topic ID where message will be submitted.
     */
    HcsDidTransaction(MessageEnvelope<HcsDidMessage> message, TopicId topicId) throws DidError {
        if (message != null && topicId != null) {
            this.topicId = topicId;
            this.message = message;
            this.executed = false;
        } else {
            throw new DidError("Invalid arguments");
        }
    }

    /**
     * Provides a {@link HcsDidTopicListener} instance specific to the submitted message type.
     *
     * @param topicIdToListen ID of the HCS topic.
     * @return The topic listener for this message on a mirror node.
     */
    protected HcsDidTopicListener provideTopicListener(TopicId topicIdToListen) {
        return new HcsDidTopicListener(topicIdToListen);
    }

    /**
     * Handles the error.
     * If external error handler is defined, passes the error there, otherwise raises RuntimeException.
     *
     * @param err The error.
     * @throws RuntimeException Runtime exception with the given error in case external error handler is not defined.
     */
    protected void handleError(final Throwable err) throws DidError {
        if (this.errorHandler != null) {
            errorHandler.accept(err);
        } else {
            throw new DidError(err.getMessage());
        }
    }

    /**
     * Handles event from a mirror node when a message was consensus was reached and message received.
     *
     * @param receiver The receiver handling incoming message.
     * @return This transaction instance.
     */
    public HcsDidTransaction onMessageConfirmed(final Consumer<MessageEnvelope<HcsDidMessage>> receiver) {
        this.receiver = receiver;
        return this;
    }


    /**
     * Defines a handler for errors when they happen during execution.
     *
     * @param handler The error handler.
     * @return This transaction instance.
     */
    public HcsDidTransaction onError(final Consumer<Throwable> handler) {
        this.errorHandler = handler;
        return this;
    }

    /**
     * Defines a function that signs the message.
     *
     * @param signer The signing function to set.
     * @return This transaction instance.
     */
    public HcsDidTransaction signMessage(final UnaryOperator<byte[]> signer) {
        this.signer = signer;
        return this;
    }


    /**
     * Sets {@link TopicMessageSubmitTransaction} parameters, builds and signs it without executing it.
     * Topic ID and transaction message content are already set in the incoming transaction.
     *
     * @param builderFunction The transaction builder function.
     * @return This transaction instance.
     */
    public HcsDidTransaction buildAndSignTransaction(
            final Function<TopicMessageSubmitTransaction, Transaction> builderFunction) {
        this.buildTransactionFunction = builderFunction;
        return this;
    }

    /**
     * Runs validation logic.
     *
     * @param validator The errors validator.
     */
    protected void validate(final Validator validator) {
        validator.require(!executed, "This transaction has already been executed.");
        // signing function or signing key is only needed if signed message was not provided.
        validator.require((signer != null)
                        || ((message != null) && !Strings.isNullOrEmpty(message.getSignature())),
                "Signing function is missing.");
        validator.require(buildTransactionFunction != null, "Transaction builder is missing.");

    }


    /**
     * Builds the message and submits it to appnet's topic.
     *
     * @param client The hedera network client.
     * @return Transaction ID.
     */
    public TransactionId execute(final Client client) throws JsonProcessingException, DidError {
        new Validator().checkValidationErrors("MessageTransaction execution failed: ", this::validate);

        MessageEnvelope<HcsDidMessage> envelope = this.message;
        byte[] messageContent = envelope.getSignature() == null ? envelope.toJSON().getBytes(StandardCharsets.UTF_8) : envelope.sign(signer);


        if (receiver != null) {
            listener = provideTopicListener(topicId);
            byte[] finalMessageContent = messageContent;
            listener.setStartTime(Instant.now().minusSeconds(1))
                    .setIgnoreErrors(false)
                    .addFilter(r -> Arrays.equals(finalMessageContent, r.contents))
                    .onError(err -> {
                        try {
                            handleError(err);
                        } catch (DidError e) {
                            e.printStackTrace();
                        }
                    })
                    .onInvalidMessageReceived((response, reason) -> {
                        // Consider only the message submitted.
                        if (!Arrays.equals(finalMessageContent, response.contents)) {
                            return;
                        }

                        // Report error and stop listening
                        try {
                            handleError(new DidError(reason + ": " + new String(response.contents)));
                        } catch (DidError e) {
                            listener.unsubscribe();
                            e.printStackTrace();
                        }

                    })
                    .subscribe(client, msg -> {
                        listener.unsubscribe();
                        receiver.accept(msg);
                    });
        }

        TopicMessageSubmitTransaction tx = new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage(messageContent);


        return getTransactionId(client, tx);
    }


    private TransactionId getTransactionId(final Client client, final TopicMessageSubmitTransaction tx) throws DidError {
        TransactionId transactionId = null;
        try {
            TransactionResponse response;
            if (buildTransactionFunction != null) {
                response = (TransactionResponse) buildTransactionFunction
                        .apply(tx)
                        .execute(client);
            } else {
                response = tx.execute(client);
            }
            transactionId = response.transactionId;
            executed = true;
        } catch (PrecheckStatusException | TimeoutException e) {
            handleError(e);
            if (listener != null) {
                listener.unsubscribe();
            }
        }

        return transactionId;
    }


}
