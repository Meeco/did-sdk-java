package com.hedera.hashgraph.identity.hcs.did;

import com.hedera.hashgraph.identity.DidDocumentBase;
import com.hedera.hashgraph.identity.DidMethodOperation;
import com.hedera.hashgraph.identity.hcs.AesEncryptionUtil;
import com.hedera.hashgraph.identity.hcs.MessageEnvelope;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TopicId;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests DID message construction and validation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HcsDidMessageTest {
  private static final TopicId DID_TOPIC_ID1 = TopicId.fromString("0.0.2");
  private static final TopicId DID_TOPIC_ID2 = TopicId.fromString("0.0.3");
  private Dotenv dotenv = Dotenv.configure().ignoreIfMissing().ignoreIfMalformed().load();
  // Grab the network to use from environment variables
  private String network = Objects.requireNonNull(dotenv.get("NETWORK"));

  @Test
  void testValidMessage() {
    PrivateKey privateKey = HcsDid.generateDidRootKey();
    HcsDid did = new HcsDid(network, privateKey.getPublicKey());
    DidDocumentBase doc = did.generateDidDocument();
    String didJson = doc.toJson();
    MessageEnvelope<HcsDidMessage> originalEnvelope = HcsDidMessage.fromDidDocumentJson(didJson,
            DidMethodOperation.CREATE);
    byte[] message = originalEnvelope.sign(msg -> privateKey.sign(msg));

    MessageEnvelope<HcsDidMessage> envelope = MessageEnvelope
            .fromJson(new String(message, StandardCharsets.UTF_8), HcsDidMessage.class);

    assertTrue(envelope.isSignatureValid(e -> e.open().extractDidRootKey()));
    // Test below should be true, as the did does not contain tid parameter
//    assertTrue(envelope.open().isValid(DID_TOPIC_ID1));
    assertEquals(originalEnvelope.open().getTimestamp(), envelope.open().getTimestamp());
  }

  @Test
  void testEncryptedMessage() {
    final String secret = "Secret encryption password";

    PrivateKey privateKey = HcsDid.generateDidRootKey();
    HcsDid did = new HcsDid(network, privateKey.getPublicKey());
    DidDocumentBase doc = did.generateDidDocument();
    String didJson = doc.toJson();

    MessageEnvelope<HcsDidMessage> originalEnvelope = HcsDidMessage.fromDidDocumentJson(didJson,
            DidMethodOperation.CREATE);

    MessageEnvelope<HcsDidMessage> encryptedMsg = originalEnvelope
            .encrypt(HcsDidMessage.getEncrypter(m -> AesEncryptionUtil.encrypt(m, secret)));

    MessageEnvelope<HcsDidMessage> encryptedSignedMsg = MessageEnvelope
            .fromJson(new String(encryptedMsg.sign(m -> privateKey.sign(m)), StandardCharsets.UTF_8),
                    HcsDidMessage.class);

    assertNotNull(encryptedSignedMsg);
    // Throw error if decrypter is not provided
    assertThrows(IllegalArgumentException.class, () -> encryptedSignedMsg.open());

    // Decrypt and open message
    HcsDidMessage decryptedMsg = encryptedSignedMsg
            .open(HcsDidMessage.getDecrypter((m, i) -> AesEncryptionUtil.decrypt(m, secret)));

    // Check if it's properties are correct after decryption
    assertNotNull(decryptedMsg);
    assertEquals(originalEnvelope.open().getDidDocumentBase64(), decryptedMsg.getDidDocumentBase64());
    assertEquals(originalEnvelope.open().getDid(), decryptedMsg.getDid());
  }

  @Test
  void testInvalidDid() {
    PrivateKey privateKey = HcsDid.generateDidRootKey();
    HcsDid did = new HcsDid(network, privateKey.getPublicKey());
    DidDocumentBase doc = did.generateDidDocument();

    String didJson = doc.toJson();
    byte[] message = HcsDidMessage
            .fromDidDocumentJson(didJson, DidMethodOperation.CREATE)
            .sign(msg -> privateKey.sign(msg));

    HcsDidMessage msg = MessageEnvelope
            .fromJson(new String(message, StandardCharsets.UTF_8), HcsDidMessage.class)
            .open();

    HcsDid differentDid = new HcsDid(network, HcsDid.generateDidRootKey().getPublicKey());
    msg.did = differentDid.toDid();

    assertFalse(msg.isValid());
  }

  @Test
  void testInvalidTopic() {
    PrivateKey privateKey = HcsDid.generateDidRootKey();
    // Include topic ID in the DID.
    HcsDid did = new HcsDid(network, privateKey.getPublicKey(), DID_TOPIC_ID1);
    DidDocumentBase doc = did.generateDidDocument();

    String didJson = doc.toJson();
    byte[] message = HcsDidMessage
            .fromDidDocumentJson(didJson, DidMethodOperation.CREATE)
            .sign(msg -> privateKey.sign(msg));

    HcsDidMessage msg = MessageEnvelope
            .fromJson(new String(message, StandardCharsets.UTF_8), HcsDidMessage.class)
            .open();

    assertTrue(msg.isValid(DID_TOPIC_ID1));
    //assertFalse(msg.isValid(DID_TOPIC_ID2));
  }

  @Test
  void testMissingData() {
    PrivateKey privateKey = HcsDid.generateDidRootKey();
    HcsDid did = new HcsDid(network, privateKey.getPublicKey());
    DidDocumentBase doc = did.generateDidDocument();
    final DidMethodOperation operation = DidMethodOperation.CREATE;

    String didJson = doc.toJson();
    byte[] message = HcsDidMessage
            .fromDidDocumentJson(didJson, DidMethodOperation.CREATE)
            .sign(msg -> privateKey.sign(msg));

    HcsDidMessage validMsg = MessageEnvelope
            .fromJson(new String(message, StandardCharsets.UTF_8), HcsDidMessage.class)
            .open();

    HcsDidMessage msg = new HcsDidMessage(operation, null, validMsg.getDidDocumentBase64());
    assertFalse(msg.isValid());

    msg = new HcsDidMessage(operation, validMsg.getDid(), null);
    assertFalse(msg.isValid());
    assertNull(msg.getDidDocument());
    assertNotNull(msg.getDid());
    assertEquals(operation, msg.getOperation());
  }

  @Test
  void testInvalidSignature() {
    PrivateKey privateKey = HcsDid.generateDidRootKey();
    HcsDid did = new HcsDid(network, privateKey.getPublicKey());
    DidDocumentBase doc = did.generateDidDocument();

    String didJson = doc.toJson();
    // Sign message with different key.
    byte[] message = HcsDidMessage
            .fromDidDocumentJson(didJson, DidMethodOperation.CREATE)
            .sign(msg -> HcsDid.generateDidRootKey().sign(msg));

    MessageEnvelope<HcsDidMessage> envelope = MessageEnvelope
            .fromJson(new String(message, StandardCharsets.UTF_8), HcsDidMessage.class);

    assertFalse(envelope.isSignatureValid(e -> e.open().extractDidRootKey()));
  }
}
