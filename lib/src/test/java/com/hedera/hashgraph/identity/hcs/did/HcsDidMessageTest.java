package com.hedera.hashgraph.identity.hcs.did;

import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.DidMethodOperation;
import com.hedera.hashgraph.identity.hcs.did.event.owner.HcsDidCreateDidOwnerEvent;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TopicId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
public class HcsDidMessageTest {

    final String network = "testnet";
    final TopicId DID_TOPIC_ID1 = TopicId.fromString("0.0.2");
    final TopicId DID_TOPIC_ID2 = TopicId.fromString("0.0.3");

    Client client = Client.forTestnet();
    PrivateKey privateKey = PrivateKey.generateED25519();
    String identifier = String.format("did:hedera:%s:%s_%s", network, Hashing.Multibase.encode(privateKey.getPublicKey().toBytes()), DID_TOPIC_ID1);

    @Test
    void testValidMessage() throws DidError {
        HcsDid did = new HcsDid(identifier, privateKey, client);

        HcsDidMessage message = new HcsDidMessage(
                DidMethodOperation.CREATE,
                did.getIdentifier(),
                new HcsDidCreateDidOwnerEvent(
                        did.getIdentifier() + "#did-root-key",
                        did.getIdentifier(),
                        privateKey.getPublicKey()
                )
        );

        assertTrue(message.isValid(DID_TOPIC_ID1));
    }


    @Test
    void testInvalidDid() throws DidError {
        HcsDid did = new HcsDid(identifier, privateKey, client);

        HcsDidMessage message = new HcsDidMessage(
                DidMethodOperation.CREATE,
                "invalid_did###",
                new HcsDidCreateDidOwnerEvent(
                        did.getIdentifier() + "#did-root-key",
                        did.getIdentifier(),
                        privateKey.getPublicKey()
                )
        );

        assertFalse(message.isValid());
    }


    @Test
    void testInvalidTopic() throws DidError {
        HcsDid did = new HcsDid(identifier, privateKey, client);

        HcsDidMessage message = new HcsDidMessage(
                DidMethodOperation.CREATE,
                did.getIdentifier(),
                new HcsDidCreateDidOwnerEvent(
                        did.getIdentifier() + "#did-root-key",
                        did.getIdentifier(),
                        privateKey.getPublicKey()
                )
        );

        assertTrue(message.isValid(DID_TOPIC_ID1));
        assertFalse(message.isValid(DID_TOPIC_ID2));
    }


    @Test
    void testMissingData() throws DidError {
        HcsDid did = new HcsDid(identifier, privateKey, client);

        HcsDidMessage message = new HcsDidMessage(
                null,
                did.getIdentifier(),
                new HcsDidCreateDidOwnerEvent(
                        did.getIdentifier() + "#did-root-key",
                        did.getIdentifier(),
                        privateKey.getPublicKey()
                )
        );

        assertNull(message.getOperation());
        assertFalse(message.isValid());

        message = new HcsDidMessage(
                DidMethodOperation.CREATE,
                null,
                new HcsDidCreateDidOwnerEvent(
                        did.getIdentifier() + "#did-root-key",
                        did.getIdentifier(),
                        privateKey.getPublicKey()
                )
        );

        assertNull(message.getDid());
        assertFalse(message.isValid());

        message = new HcsDidMessage(DidMethodOperation.CREATE, did.getIdentifier(), null);

        assertNull(message.getEvent());
        assertFalse(message.isValid());

        message = new HcsDidMessage(
                DidMethodOperation.CREATE,
                did.getIdentifier(),
                new HcsDidCreateDidOwnerEvent(
                        did.getIdentifier() + "#did-root-key",
                        did.getIdentifier(),
                        privateKey.getPublicKey()
                )
        );

        assertTrue(message.isValid());


    }


}
