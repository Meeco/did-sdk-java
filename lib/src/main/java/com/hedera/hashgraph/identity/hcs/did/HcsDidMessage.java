package com.hedera.hashgraph.identity.hcs.did;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.identity.DidMethodOperation;
import com.hedera.hashgraph.identity.DidParser;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEvent;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventParser;
import com.hedera.hashgraph.sdk.TopicId;
import org.threeten.bp.Instant;

import java.util.Optional;

public class HcsDidMessage {

    protected Instant timestamp;
    protected DidMethodOperation operation;
    protected String did;
    protected HcsDidEvent event;

    /**
     * Creates a new instance of {@link HcsDidMessage}.
     *
     * @param operation The operation on DID document.
     * @param did       The DID string.
     * @param event     The DID Event.
     */
    public HcsDidMessage(DidMethodOperation operation, String did, HcsDidEvent event) {
        this.timestamp = Instant.now();
        this.operation = operation;
        this.did = did;
        this.event = event;
    }

    public static HcsDidMessage fromJsonTree(JsonNode tree, Optional<HcsDidMessage> result) {
        HcsDidEvent event = HcsDidEventParser.fromBase64(DidMethodOperation.valueOf(tree.get("operation").textValue()), tree.get("event").textValue());

        HcsDidMessage hcsDidMessage;
        if (result.isEmpty()) {
            hcsDidMessage = new HcsDidMessage(DidMethodOperation.valueOf(tree.get("operation").textValue()), tree.get("did").textValue(), event);
        } else {
            hcsDidMessage = result.get();
            hcsDidMessage.operation = DidMethodOperation.valueOf(tree.get("operation").textValue());
            hcsDidMessage.did = tree.get("did").textValue();
            hcsDidMessage.event = event;
        }
        hcsDidMessage.timestamp = Instant.parse(tree.get("timestamp").textValue());
        return hcsDidMessage;
    }

    public static HcsDidMessage fromJson(String json) throws JsonProcessingException {
        return HcsDidMessage.fromJsonTree(new ObjectMapper().readTree(json), null);
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public DidMethodOperation getOperation() {
        return this.operation;
    }

    public String getDid() {
        return this.did;
    }

    public HcsDidEvent getEvent() {
        return this.event;
    }

    public String getEventBase64() {
        return this.getEvent().getBase64();
    }

    /**
     * Validates this DID message by checking its completeness, signature and DID document.
     *
     * @param topicId The DID topic ID against which the message is validated.
     * @return True if the message is valid, false otherwise.
     */
    public boolean isValid(Optional<TopicId> topicId) {
        TopicId didTopicId = topicId.orElse(null);

        if (this.did == null || this.event == null || this.operation == null) {
            return false;
        }

        try {
            HcsDid hcsDid = DidParser.parse(this.did);

            // Verify that the message was sent to the right topic, if the DID contains the topic
            if (didTopicId != null && hcsDid.getTopicId() != null && !didTopicId.equals(hcsDid.getTopicId())) {
                return false;
            }
        } catch (Exception e) {
            return false;

        }

        return true;
    }

    public JsonNode toJsonTree() throws JsonProcessingException {
        return new ObjectMapper().readTree("{\"timestamp\":" + this.getTimestamp().toString() + ",\"operation\":" + this.operation + ",\"did\":" + this.did + ", \"event\":" + this.getEventBase64() + "}");
 
    }

    public String toJSON() throws JsonProcessingException {
        return this.toJsonTree().toString();
    }
}
