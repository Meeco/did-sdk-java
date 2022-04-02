package com.hedera.hashgraph.identity.hcs.did;

import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TopicId;
import org.javatuples.Triplet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class HcsDidTest {

    @Test
    @DisplayName("throws error because of missing identifier and privateKey")
    void testErrorWhenMissingIdentifierAndPk() throws DidError {
        Assertions.assertThrowsExactly(DidError.class, () -> new HcsDid(null, null, null), "identifier and privateKey cannot both be empty");
    }

    @Test
    @DisplayName("successfully builds HcsDid with private key only")
    void testBuildWithPk() throws DidError {
        PrivateKey privateKey = PrivateKey.generateED25519();
        HcsDid did = new HcsDid(null, privateKey, null);

        Assertions.assertNull(did.getIdentifier());
        Assertions.assertEquals(privateKey, did.getPrivateKey());
        Assertions.assertNull(did.getClient());
        Assertions.assertNull(did.getTopicId());
        Assertions.assertNull(did.getNetwork());

    }

    @Test
    @DisplayName("throws error if passed identifier is invalid")
    void testIdentifier() {

        for (String s : new String[]{
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
                "did:notHedera:testnet:z6Mk8LjUL78kFVnWV9rFnNCTE5bZdRmjm2obqJwS892jVLak_0.0.1"}) {

            Assertions.assertThrows(Exception.class, () -> new HcsDid(s, null, null));

        }

    }

    @Test
    @DisplayName("accepts client parameter")
    void testAcceptClientParam() throws DidError {
        var client = Client.forTestnet();
        var identifier = "did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.29613327";
        var did = new HcsDid(identifier, null, client);

        Assertions.assertEquals(identifier, did.getIdentifier());
        Assertions.assertNull(did.getPrivateKey());
        Assertions.assertEquals(client, did.getClient());
        Assertions.assertEquals("0.0.29613327", did.getTopicId().toString());
        Assertions.assertEquals("testnet", did.getNetwork());

    }


    @Test
    @DisplayName("throws error if topicId missing")
    void throwsErrorWhenTopicIdMissing() {
        Assertions.assertThrowsExactly(DidError.class, () -> HcsDid.parseIdentifier("did:hedera:testnet:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk"), "DID string is invalid: topic ID is missing");

    }

    @Test
    @DisplayName("throws error if invalid prefix")
    void throwsErrorIfInvalidPrefix() {
        Assertions.assertThrowsExactly(DidError.class, () -> HcsDid.parseIdentifier("abcd:hedera:testnet:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk_0.0.1"), "DID string is invalid: invalid prefix.");

    }

    @Test
    @DisplayName("throws error if invalid method name")
    void throwsErrorIfInvalidMethodName() {
        Assertions.assertThrowsExactly(DidError.class, () -> HcsDid.parseIdentifier("did:hashgraph:testnet:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk_0.0.1"), "DID string is invalid: invalid method name: hashgraph");

    }


    @Test
    @DisplayName("throws error if Invalid Hedera network")
    void throwsErrorIfInvalidNetwork() {
        Assertions.assertThrowsExactly(DidError.class, () -> HcsDid.parseIdentifier("did:hedera:nonetwork:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk_0.0.1"), "DID string is invalid. Invalid Hedera network.");

    }


    @Test
    @DisplayName("throws error if Invalid id string")
    void throwsErrorIfInvalidString() {
        Assertions.assertThrowsExactly(DidError.class, () -> HcsDid.parseIdentifier("did:hedera:testnet:z6Mkabcd_0.0.1"), "DID string is invalid. ID holds incorrect format.");

    }

    @Test
    @DisplayName("should get array with NetworkName, topicId and didIdString")
    void shouldParseArrayWithNetworkTopicIdAndIdString() throws DidError {

        Triplet<String, TopicId, String> triplet = HcsDid.parseIdentifier(
                "did:hedera:testnet:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk_0.0.1"
        );

        Assertions.assertEquals("testnet", triplet.getValue0());
        Assertions.assertEquals("0.0.1", triplet.getValue1().toString());
        Assertions.assertEquals("z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk", triplet.getValue2());

    }

    @Test
    @DisplayName("should get DID Id String from publicKey")
    void ShouldGetDidIdStringFromPublicKey() {
        var privateKey = PrivateKey.fromString(
                "302e020100300506032b657004220420a4b76d7089dfd33c83f586990c3a36ae92fb719fdf262e7749d1b0ddd1d055b0"
        );
        var result = HcsDid.publicKeyToIdString(privateKey.getPublicKey());
        Assertions.assertEquals("z6MkvD6JAfMyP6pgQoYxfE9rubgwLD9Hmz8rQh1FAxvbW8XB", result);
    }

    @Test
    @DisplayName("should get publicKey from DID Id String")
    void ShouldGetPublicKeyFromDidIdString() {
        var privateKey = PrivateKey.fromString(
                "302e020100300506032b657004220420a4b76d7089dfd33c83f586990c3a36ae92fb719fdf262e7749d1b0ddd1d055b0"
        );
        var result = HcsDid.stringToPublicKey("z6MkvD6JAfMyP6pgQoYxfE9rubgwLD9Hmz8rQh1FAxvbW8XB");
        Assertions.assertEquals(privateKey.getPublicKey(), result);

    }

}
