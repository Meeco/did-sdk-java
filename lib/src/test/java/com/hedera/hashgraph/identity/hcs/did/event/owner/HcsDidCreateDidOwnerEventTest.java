package com.hedera.hashgraph.identity.hcs.did.event.owner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
public class HcsDidCreateDidOwnerEventTest {

    PrivateKey privateKey = PrivateKey.fromString(
            "302e020100300506032b6570042204209044d8f201e4b0aa7ba8ed577b0334b8cb6e38aad6c596171b5b1246737f5079"
    );
    String identifier = "did:hedera:testnet:" + Hashing.Multibase.encode(privateKey.getPublicKey().toBytes()) + "_0.0.29613327";
    HcsDidCreateDidOwnerEvent event = new HcsDidCreateDidOwnerEvent(identifier + "#did-root-key", identifier, privateKey.getPublicKey());

    public HcsDidCreateDidOwnerEventTest() throws DidError {
    }

    @Test
    void itTargetsService() {
        assertEquals(event.getTargetName(), HcsDidEventTargetName.DID_OWNER);
    }

    @Test
    void itThrowsErrorIfIdIsNULL() {
        try {
            new HcsDidCreateDidOwnerEvent(null, identifier, privateKey.getPublicKey());
        } catch (DidError error) {
            assertEquals("Validation failed. DID Owner args are missing", error.getMessage());
        }
    }

    @Test
    void itThrowsErrorIfTypeIsNULL() {
        try {
            new HcsDidCreateDidOwnerEvent(identifier + "#did-root-key", null, privateKey.getPublicKey());
        } catch (DidError error) {
            assertEquals("Validation failed. DID Owner args are missing", error.getMessage());
        }
    }

    @Test
    void itThrowsErrorIfPublicKeyIsNULL() {
        try {
            new HcsDidCreateDidOwnerEvent(identifier + "#did-root-key", identifier, null);
        } catch (DidError error) {
            assertEquals("Validation failed. DID Owner args are missing", error.getMessage());
        }
    }

    @Test
    void itThrowsErrorIfIdIsNotValid() {
        try {
            new HcsDidCreateDidOwnerEvent(identifier, identifier, privateKey.getPublicKey());
        } catch (DidError error) {
            assertEquals("Event ID is invalid. Expected format: {did}#did-root-key", error.getMessage());
        }
    }

    @Test
    void getId() {
        assertEquals(identifier + "#did-root-key", event.getId());
    }

    @Test
    void getType() {
        assertEquals("Ed25519VerificationKey2018", event.getType());
    }

    @Test
    void getController() {
        assertEquals(identifier, event.getController());
    }

    @Test
    void getPublicKey() {
        assertEquals(privateKey.getPublicKey(), event.getPublicKey());
    }

    @Test
    void getPublicKeyMultibase() {
        assertEquals("z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa", event.getPublicKeyMultibase());
    }

    @Test
    void getBase64() {
        assertEquals(
                "eyJESURPd25lciI6eyJpZCI6ImRpZDpoZWRlcmE6dGVzdG5ldDp6Nk1rb2dWem9HSk1WVkxoYXo4MmNBNWpaUUtBQXFVZ2hoQ3JwemtTREZEd3hmSmFfMC4wLjI5NjEzMzI3I2RpZC1yb290LWtleSIsInR5cGUiOiJFZDI1NTE5VmVyaWZpY2F0aW9uS2V5MjAxOCIsImNvbnRyb2xsZXIiOiJkaWQ6aGVkZXJhOnRlc3RuZXQ6ejZNa29nVnpvR0pNVlZMaGF6ODJjQTVqWlFLQUFxVWdoaENycHprU0RGRHd4ZkphXzAuMC4yOTYxMzMyNyIsInB1YmxpY0tleU11bHRpYmFzZSI6Ino2TWtvZ1Z6b0dKTVZWTGhhejgyY0E1alpRS0FBcVVnaGhDcnB6a1NERkR3eGZKYSJ9fQ=="
                , event.getBase64());
    }

    @Test
    void toJsonTree() throws JsonProcessingException {

        String jsonString = "{\"DIDOwner\":{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"}}";
        JsonNode ownerEventJsonNode = new ObjectMapper().readTree(jsonString);

        assertEquals(ownerEventJsonNode, event.toJsonTree());
    }

    @Test
    void toJSON() {
        assertEquals("{\"DIDOwner\":{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"}}",
                event.toJSON());
    }

    @Test
    void fromJsonTree() throws JsonProcessingException, DidError {
        String jsonString = "{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"}";
        JsonNode ownerEventJsonNode = new ObjectMapper().readTree(jsonString);

        HcsDidCreateDidOwnerEvent eventFromJson = HcsDidCreateDidOwnerEvent.fromJsonTree(ownerEventJsonNode);


        String expectedJsonString = "{\"DIDOwner\":{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"}}";
        JsonNode expected = new ObjectMapper().readTree(expectedJsonString);

        assertEquals(expected, eventFromJson.toJsonTree());
        assertEquals(expectedJsonString, eventFromJson.toJSON());
    }
}
