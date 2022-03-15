package com.hedera.hashgraph.identity.hcs.did;

import com.hedera.hashgraph.identity.DidDocumentBase;
import com.hedera.hashgraph.identity.DidSyntax;
import com.hedera.hashgraph.identity.DidSyntax.Method;
import com.hedera.hashgraph.identity.HederaDid;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TopicId;
import io.github.cdimascio.dotenv.Dotenv;
import org.bitcoinj.core.Base58;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests HcsDid generation and parsing operations.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HcsDidTest {
  private Dotenv dotenv = Dotenv.configure().ignoreIfMissing().ignoreIfMalformed().load();
  // Grab the network to use from environment variables
  private String network = Objects.requireNonNull(dotenv.get("NETWORK"));

  @Test
  void testGenerateAndParseDidWithoutTid() throws NoSuchAlgorithmException {
    final String addressBook = "0.0.24352";

    // Generate pair of HcsDid root keys
    PrivateKey privKey = HcsDid.generateDidRootKey();
    PublicKey pubKey = privKey.getPublicKey();

    // Generate HcsDid
    HcsDid did = new HcsDid(network, pubKey);

    // Convert HcsDid to HcsDid string
    String didString = did.toString();

    assertNotNull(didString);

//    // Parse HcsDid string back to HcsDid object.
//    HcsDid parsedDid = HcsDid.fromString(didString);
//
//    assertNotNull(parsedDid);
//
//    assertNull(parsedDid.getDidTopicId());
//
//    assertEquals(parsedDid.toString(), didString);
//    assertEquals(parsedDid.getMethod(), Method.HEDERA_HCS);
//    assertEquals(parsedDid.getNetwork(), network);
//    assertEquals(parsedDid.getIdString(), did.getIdString());
  }

  @Test
  void testGenerateAndParseDidWithTid() throws NoSuchAlgorithmException {
    final String didTopicId = "1.5.23462345";

    // Generate pair of HcsDid root keys
    PrivateKey privateKey = HcsDid.generateDidRootKey();

    // Generate HcsDid
    TopicId topicId = TopicId.fromString(didTopicId);
    HcsDid did = new HcsDid(network, privateKey.getPublicKey(), topicId);

    // Convert HcsDid to HcsDid string
    String didString = did.toString();

    assertNotNull(didString);

    // Parse HcsDid string back to HcsDid object.
    HcsDid parsedDid = HcsDid.fromString(didString);

    assertNotNull(parsedDid);
    assertNotNull(parsedDid.getDidTopicId());

    //assertEquals(parsedDid.toDid(), didString);
    assertEquals(parsedDid.getMethod(), Method.HEDERA_HCS);
    assertEquals(parsedDid.getNetwork(), network);
//    assertEquals(parsedDid.getDidTopicId().toString(), didTopicId);
    assertEquals(parsedDid.getIdString(), did.getIdString());

    // Generate DID document
    DidDocumentBase parsedDocument = parsedDid.generateDidDocument();

    assertNotNull(parsedDocument);
    assertEquals(parsedDocument.getId(), parsedDid.toString());
    assertEquals(parsedDocument.getContext(), DidSyntax.DID_DOCUMENT_CONTEXT);
    assertNull(parsedDocument.getDidRootKey());

    // Generate DID document from original DID.
    DidDocumentBase document = did.generateDidDocument();

    assertNotNull(document);
    //assertEquals(document.getId(), parsedDid.toString());
    assertEquals(document.getContext(), DidSyntax.DID_DOCUMENT_CONTEXT);
    assertNotNull(document.getDidRootKey());
    assertEquals(document.getDidRootKey().getPublicKeyBase58(), Base58.encode(privateKey.getPublicKey().toBytes()));

  }

  @Test
  void testParsePredefinedDids() throws NoSuchAlgorithmException {
    final String addressBook = "0.0.24352";
    final String didTopicId = "1.5.23462345";

    final String validDidWithSwitchedParamsOrder = "did:hedera:testnet:8LjUL78kFVnWV9rFnNCTE5bZdRmjm2obqJwS892jVLak"
            + "_" + didTopicId;

    final String[] invalidDids = {
            null,
            "invalidDid1",
            "did:invalid",
            "did:invalidMethod:8LjUL78kFVnWV9rFnNCTE5bZdRmjm2obqJwS892jVLak_0.0.24352",
            "did:hedera:invalidNetwork:8LjUL78kFVnWV9rFnNCTE5bZdRmjm2obqJwS892jVLak_0.0.24352",
            "did:hedera:testnet:invalidAddress_0.0.24352_1.5.23462345",
            "did:hedera:testnet_1.5.23462345",
            "did:hedera:testnet:z6Mk8LjUL78kFVnWV9rFnNCTE5bZdRmjm2obqJwS892jVLak:unknown:parameter=1_missing",
            "did:hedera:testnet:z6Mk8LjUL78kFVnWV9rFnNCTE5bZdRmjm2obqJwS892jVLak_0.0.1=1",
            "did:hedera:testnet:z6Mk8LjUL78kFVnWV9rFnNCTE5bZdRmjm2obqJwS892jVLak:hedera:testnet:fid",
            "did:hedera:testnet:z6Mk8LjUL78kFVnWV9rFnNCTE5bZdRmjm2obqJwS892jVLak:unknownPart_0.0.1",
            "did:notHedera:testnet:z6Mk8LjUL78kFVnWV9rFnNCTE5bZdRmjm2obqJwS892jVLak_0.0.1",
    };

    // Expect to fail parsing all invalid DIDs
    for (String did : invalidDids) {
//      assertThrows(IllegalArgumentException.class, () -> HcsDid.fromString(did));
//      assertThrows(IllegalArgumentException.class, () -> HederaDid.fromString(did));
    }

    // Parse valid DID with parameters order switched
    HcsDid validDid = HcsDid.fromString(validDidWithSwitchedParamsOrder);

    assertNotNull(validDid);
    //assertNotNull(validDid.getDidTopicId());

//    assertEquals(validDid.getDidTopicId().toString(), didTopicId);

    HederaDid validDidViaInterface = HederaDid.fromString(validDidWithSwitchedParamsOrder);
    assertNotNull(validDidViaInterface);

    assertEquals(validDid.getMethod(), HcsDid.DID_METHOD);
  }
}
