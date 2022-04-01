package com.hedera.hashgraph.identity.hcs.did;

import com.hedera.hashgraph.identity.hcs.MessageEnvelope;
import com.hedera.hashgraph.identity.utils.Validator;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TopicId;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class HcsDidEventMessageResolver {

    /**
     * Default time to wait before finishing resolution and after the last message was received.
     */
    public static final long DEFAULT_TIMEOUT = 300_000;
    private final AtomicLong lastMessageArrivalTime;
    private final HcsDidTopicListener listener;
    private final ScheduledExecutorService executorService;
    protected List<MessageEnvelope<HcsDidMessage>> messages = new ArrayList<>();
    protected TopicId topicId;
    private long noMoreMessagesTimeout;
    private Consumer<List<MessageEnvelope<HcsDidMessage>>> resultsHandler;
    private Consumer<Throwable> errorHandler;
    private Set<String> existingSignatures;


    /**
     * Instantiates a message resolver.
     *
     * @param topicId Consensus topic ID.
     */
    public HcsDidEventMessageResolver(final TopicId topicId) {
        this.topicId = topicId;
        this.listener = new HcsDidTopicListener(this.topicId);
        this.executorService = Executors.newScheduledThreadPool(2);
        this.noMoreMessagesTimeout = DEFAULT_TIMEOUT;
        this.lastMessageArrivalTime = new AtomicLong(System.currentTimeMillis());
    }

    public HcsDidEventMessageResolver(final TopicId topicId, Instant startTime) {
        this.topicId = topicId;
        this.listener = new HcsDidTopicListener(this.topicId, startTime);
        this.executorService = Executors.newScheduledThreadPool(2);
        this.noMoreMessagesTimeout = DEFAULT_TIMEOUT;
        this.lastMessageArrivalTime = new AtomicLong(System.currentTimeMillis());
    }

    public void execute(Client client) {
        new Validator().checkValidationErrors("Resolver not executed: ", this::validate);
        existingSignatures = new HashSet<>();

        listener.setStartTime(Instant.MIN)
                .setEndTime(Instant.now())
                .setIgnoreErrors(false)
                .onError(errorHandler)
                .subscribe(client, this::handleMessage);

        lastMessageArrivalTime.set(System.currentTimeMillis());
        waitOrFinish();
    }

    /**
     * Runs validation logic of the resolver's configuration.
     *
     * @param validator The errors validator.
     */
    protected void validate(final Validator validator) {
        validator.require(topicId != null, "Consensus topic ID not defined.");
        validator.require(resultsHandler != null, "Results handler 'whenFinished' not defined.");
    }


    /**
     * Waits for a new message from the topic for the configured amount of time.
     */
    private void waitOrFinish() {
        // Check if the task should be rescheduled as new message arrived.
        long timeDiff = System.currentTimeMillis() - lastMessageArrivalTime.get();
        if (timeDiff < noMoreMessagesTimeout) {
            Runnable finishTask = this::waitOrFinish;

            executorService.schedule(finishTask, noMoreMessagesTimeout - timeDiff, TimeUnit.MILLISECONDS);
            return;
        }

        // Finish the task
        resultsHandler.accept(this.messages);

        // Stop listening for new messages.
        if (listener != null) {
            listener.unsubscribe();
        }

        // Stop the timeout executor.
        if (executorService != null) {
            executorService.shutdown();
        }
    }


    /**
     * Handles incoming DID messages from DID Topic on a mirror node.
     *
     * @param envelope The parsed message envelope in a PLAIN mode.
     */
    private void handleMessage(final MessageEnvelope<HcsDidMessage> envelope) {
        lastMessageArrivalTime.set(System.currentTimeMillis());

        // Skip messages that are not relevant for requested DID's
        if (!matchesSearchCriteria(envelope.open())) {
            return;
        }

        // Skip duplicated messages
        if (existingSignatures.contains(envelope.getSignature())) {
            return;
        }
        existingSignatures.add(envelope.getSignature());
        this.messages.add(envelope);
    }

    /**
     * Defines a handler for resolution results.
     * This will be called when the resolution process is finished.
     *
     * @param handler The results' handler.
     * @return This resolver instance.
     */
    public HcsDidEventMessageResolver whenFinished(final Consumer<List<MessageEnvelope<HcsDidMessage>>> handler) {
        this.resultsHandler = handler;
        return this;
    }

    /**
     * Defines a handler for errors when they happen during resolution.
     *
     * @param handler The error handler.
     * @return This resolver instance.
     */
    public HcsDidEventMessageResolver onError(final Consumer<Throwable> handler) {
        this.errorHandler = handler;
        return this;
    }

    /**
     * Defines a maximum time in milliseconds to wait for new messages from the topic.
     * Default is 30 seconds.
     *
     * @param timeout The timeout in milliseconds to wait for new messages from the topic.
     * @return This resolver instance.
     */
    public HcsDidEventMessageResolver setTimeout(final long timeout) {
        this.noMoreMessagesTimeout = timeout;
        return this;
    }


    protected boolean matchesSearchCriteria(HcsDidMessage message) {
        return true;
    }


}
