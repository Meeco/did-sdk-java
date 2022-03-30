package com.hedera.hashgraph.identity.hcs.did;

import com.hedera.hashgraph.identity.hcs.MessageEnvelope;
import com.hedera.hashgraph.sdk.TopicId;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class HcsDidResolver {

    /**
     * Default time to wait before finishing resolution and after the last message was received.
     */
    public static final long DEFAULT_TIMEOUT = 30_000;

    protected TopicId topicId;
    private AtomicLong lastMessageArrivalTime;

    private Consumer<Map<String, MessageEnvelope<HcsDidMessage>>> resultsHandler;
    private Consumer<Throwable> errorHandler;
    private Set<String> existingSignatures;

    private HcsDidTopicListener listener;
    private long noMoreMessagesTimeout;


}
