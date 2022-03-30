package com.hedera.hashgraph.identity.hcs.did;

import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.MessageEnvelope;
import com.hedera.hashgraph.sdk.*;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A listener of confirmed {@link HcsDidMessage} messages from a DID topic.
 * Messages are received from a given mirror node, parsed and validated.
 */
public class HcsDidTopicListener {

    protected TopicId topicId;
    protected TopicMessageQuery query;
    protected Consumer<Throwable> errorHandler;
    protected boolean ignoreErrors;
    protected SubscriptionHandle subscriptionHandle;
    protected List<Predicate<TopicMessage>> filters;
    protected BiConsumer<TopicMessage, String> invalidMessageHandler;


    /**
     * Creates a new instance of a topic listener for the given consensus topic.
     * By default, invalid messages are ignored and errors are not.
     *
     * @param topicId The consensus topic ID.
     */
    public HcsDidTopicListener(final TopicId topicId, Optional<Instant> startTime) {
        this.topicId = topicId;
        this.query = new TopicMessageQuery().setTopicId(topicId).setStartTime(startTime.orElseGet(() -> Instant.ofEpochSecond(0)));
        this.ignoreErrors = false;

    }

    public HcsDidTopicListener onComplete(final Runnable handler) {
        this.query.setCompletionHandler(handler);
        return this;
    }

    /**
     * Adds a custom filter for topic responses from a mirror node.
     * Messages that do not pass the test are skipped before any other checks are run.
     *
     * @param filter The filter function.
     * @return This listener instance.
     */
    public HcsDidTopicListener addFilter(final Predicate<TopicMessage> filter) {
        if (filters == null) {
            filters = new ArrayList<>();
        }

        filters.add(filter);
        return this;
    }

    /**
     * Subscribes to mirror node topic messages stream.
     *
     * @param client   Mirror client instance.
     * @param receiver Receiver of parsed messages.
     * @return This listener instance.
     */
    public HcsDidTopicListener subscribe(final Client client, final Consumer<MessageEnvelope<HcsDidMessage>> receiver) {
        subscriptionHandle = query.subscribe(
                client,
                resp -> {
                    try {
                        handleResponse(resp, receiver);
                    } catch (DidError e) {
                        e.printStackTrace();
                    }
                }
        );

        return this;
    }

    /**
     * Stops receiving messages from the topic.
     */
    public void unsubscribe() {
        if (subscriptionHandle != null) {
            subscriptionHandle.unsubscribe();
        }
    }

    /**
     * Handles incoming messages from the topic on a mirror node.
     *
     * @param response Response message coming from the mirror node for the topic.
     * @param receiver Consumer of the result message.
     */
    protected void handleResponse(final TopicMessage response,
                                  final Consumer<MessageEnvelope<HcsDidMessage>> receiver) throws DidError {

        // Run external filters first
        if (filters != null) {
            for (Predicate<TopicMessage> filter : filters) {
                if (!filter.test(response)) {
                    reportInvalidMessage(response, "Message was rejected by external filter");
                    return;
                }
            }
        }

        // Extract and parse message from the response.
        MessageEnvelope<HcsDidMessage> envelope = extractMessage(response);

        // Skip encrypted messages if decrypter was not provided
        if (envelope == null) {
            reportInvalidMessage(response, "Extracting envelope from the mirror response failed");
            return;
        }

        // Check if message inside the envelope is valid and only accept it if it is.
        if (isMessageValid(envelope, response)) {
            receiver.accept(envelope);
        }
    }

    /**
     * Extracts and parses the message inside the response object into the given type.
     *
     * @param response Response message coming from the mirror node for this listener's topic.
     * @return The message inside an envelope.
     */
    protected MessageEnvelope<HcsDidMessage> extractMessage(final TopicMessage response) throws DidError {
        MessageEnvelope<HcsDidMessage> result = null;
        try {
            result = MessageEnvelope.fromMirrorResponse(response, HcsDidMessage.class);
        } catch (Exception err) {
            handleError(err);
        }

        return result;
    }

    protected boolean isMessageValid(final MessageEnvelope<HcsDidMessage> envelope,
                                     final TopicMessage response) throws DidError {
        try {

            HcsDidMessage message = envelope.open();
            if (message == null) {
                reportInvalidMessage(response, "Empty message received when opening envelope");
                return false;
            }


            if (!message.isValid(Optional.ofNullable(topicId))) {
                reportInvalidMessage(response, "Message content validation failed.");
                return false;
            }

            return true;
        } catch (Exception err) {
            handleError(err);
            reportInvalidMessage(response, "Exception while validating message: " + err.getMessage());
            return false;
        }

    }

    /**
     * Handles the given error internally.
     * If external error handler is defined, passes the error there, otherwise raises RuntimeException or ignores it
     * depending on a ignoreErrors flag.
     *
     * @param err The error.
     * @throws RuntimeException Runtime exception with the given error in case external error handler is not defined
     *                          and errors were not requested to be ignored.
     */
    protected void handleError(final Throwable err) throws DidError {

        if (errorHandler != null) {
            errorHandler.accept(err);
        } else if (!ignoreErrors) {
            throw new DidError(err.getMessage());
        }
    }

    /**
     * Reports invalid message to the handler.
     *
     * @param response The mirror response.
     * @param reason   The reason why message validation failed.
     */
    protected void reportInvalidMessage(final TopicMessage response, final String reason) {
        if (invalidMessageHandler != null) {
            invalidMessageHandler.accept(response, reason);
        }
    }

    /**
     * Defines a handler for errors when they happen during execution.
     *
     * @param handler The error handler.
     * @return This transaction instance.
     */
    public HcsDidTopicListener onError(final Consumer<Throwable> handler) {
        this.errorHandler = handler;
        return this;
    }

    /**
     * Defines a handler for invalid messages received from the topic.
     * The first parameter of the handler is the mirror response.
     * The second parameter is the reason why the message failed validation (if available).
     *
     * @param handler The invalid message handler.
     * @return This transaction instance.
     */
    public HcsDidTopicListener onInvalidMessageReceived(final BiConsumer<TopicMessage, String> handler) {
        this.invalidMessageHandler = handler;
        return this;
    }

    public HcsDidTopicListener setStartTime(final Instant startTime) {
        query.setStartTime(startTime);
        return this;
    }

    public HcsDidTopicListener setEndTime(final Instant endTime) {
        query.setEndTime(endTime);
        return this;
    }

    public HcsDidTopicListener setLimit(final long messagesLimit) {
        query.setLimit(messagesLimit);
        return this;
    }

    public HcsDidTopicListener setIgnoreErrors(final boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
        return this;
    }


}