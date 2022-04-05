package com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship;

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
public class HcsDidUpdateVerificationRelationshipEventTest {
    PrivateKey privateKey = PrivateKey.fromString(
            "302e020100300506032b6570042204209044d8f201e4b0aa7ba8ed577b0334b8cb6e38aad6c596171b5b1246737f5079"
    );
    String identifier = "did:hedera:testnet:" + Hashing.Multibase.encode(privateKey.getPublicKey().toBytes()) + "_0.0.29613327";
    HcsDidUpdateVerificationRelationshipEvent event = new HcsDidUpdateVerificationRelationshipEvent(
            identifier + "#key-1",
            VerificationRelationshipType.AUTHENTICATION,
            VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
            identifier,
            privateKey.getPublicKey()
    );

    public HcsDidUpdateVerificationRelationshipEventTest() throws DidError {
    }

    @Test
    void itTargetsService() {
        assertEquals(event.getTargetName(), HcsDidEventTargetName.VERIFICATION_RELATIONSHIP);
    }

    @Test
    void itThrowsErrorIfIdIsNULL() {
        try {
            new HcsDidUpdateVerificationRelationshipEvent(
                    null,
                    VerificationRelationshipType.AUTHENTICATION,
                    VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                    identifier,
                    privateKey.getPublicKey()
            );
        } catch (DidError error) {
            assertEquals("Validation failed. Verification Relationship args are missing", error.getMessage());
        }
    }

    @Test
    void itThrowsErrorIfRelationshipTypeIsNULL() {
        try {
            new HcsDidUpdateVerificationRelationshipEvent(
                    identifier + "#key-1",
                    null,
                    VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                    identifier,
                    privateKey.getPublicKey()
            );

        } catch (DidError error) {
            assertEquals("Validation failed. Verification Relationship args are missing", error.getMessage());
        }
    }

    @Test
    void itThrowsErrorIfTypeIsNULL() {
        try {
            new HcsDidUpdateVerificationRelationshipEvent(
                    identifier + "#key-1",
                    VerificationRelationshipType.AUTHENTICATION,
                    null,
                    identifier,
                    privateKey.getPublicKey()
            );

        } catch (DidError error) {
            assertEquals("Validation failed. Verification Relationship args are missing", error.getMessage());
        }
    }

    @Test
    void itThrowsErrorIfControllerIsNULL() {
        try {
            new HcsDidUpdateVerificationRelationshipEvent(
                    identifier + "#key-1",
                    VerificationRelationshipType.AUTHENTICATION,
                    VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                    null,
                    privateKey.getPublicKey()
            );
        } catch (DidError error) {
            assertEquals("Validation failed. Verification Relationship args are missing", error.getMessage());
        }
    }

    @Test
    void itThrowsErrorIfPublicKeyIsNotValid() {
        try {
            new HcsDidUpdateVerificationRelationshipEvent(
                    identifier + "#key-1",
                    VerificationRelationshipType.AUTHENTICATION,
                    VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                    identifier,
                    null
            );
        } catch (DidError error) {
            assertEquals("Validation failed. Verification Relationship args are missing", error.getMessage());
        }
    }


    @Test
    void itThrowsErrorIfIdIsNotValid() {
        try {
            new HcsDidUpdateVerificationRelationshipEvent(
                    identifier,
                    VerificationRelationshipType.AUTHENTICATION,
                    VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                    identifier,
                    privateKey.getPublicKey()
            );
        } catch (DidError error) {
            assertEquals("Event ID is invalid. Expected format: {did}#key-{integer}", error.getMessage());
        }
    }

    @Test
    void getId() {
        assertEquals("did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#key-1", event.getId());
    }

    @Test
    void getRelationshipType() {
        assertEquals("authentication", event.getRelationshipType().toString());

    }

    @Test
    void getType() {
        assertEquals("Ed25519VerificationKey2018", event.getType().toString());

    }

    @Test
    void getController() {
        assertEquals("did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327", event.getController());
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
                "eyJWZXJpZmljYXRpb25SZWxhdGlvbnNoaXAiOnsiaWQiOiJkaWQ6aGVkZXJhOnRlc3RuZXQ6ejZNa29nVnpvR0pNVlZMaGF6ODJjQTVqWlFLQUFxVWdoaENycHprU0RGRHd4ZkphXzAuMC4yOTYxMzMyNyNrZXktMSIsInJlbGF0aW9uc2hpcFR5cGUiOiJhdXRoZW50aWNhdGlvbiIsInR5cGUiOiJFZDI1NTE5VmVyaWZpY2F0aW9uS2V5MjAxOCIsImNvbnRyb2xsZXIiOiJkaWQ6aGVkZXJhOnRlc3RuZXQ6ejZNa29nVnpvR0pNVlZMaGF6ODJjQTVqWlFLQUFxVWdoaENycHprU0RGRHd4ZkphXzAuMC4yOTYxMzMyNyIsInB1YmxpY0tleU11bHRpYmFzZSI6Ino2TWtvZ1Z6b0dKTVZWTGhhejgyY0E1alpRS0FBcVVnaGhDcnB6a1NERkR3eGZKYSJ9fQ==",
                event.getBase64()
        );
    }

    @Test
    void toJsonTree() throws JsonProcessingException {
        String jsonString = "{\"VerificationRelationship\":{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#key-1\",\"relationshipType\":\"authentication\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"}}";
        JsonNode verificationRelationshipEventJsonNode = new ObjectMapper().readTree(jsonString);

        assertEquals(verificationRelationshipEventJsonNode, event.toJsonTree());
    }

    @Test
    void toJSON() {
        assertEquals("{\"VerificationRelationship\":{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#key-1\",\"relationshipType\":\"authentication\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"}}",
                event.toJSON());
    }


    @Test
    void fromJsonTree() throws JsonProcessingException, DidError {
        String jsonString = "{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#key-1\",\"relationshipType\":\"authentication\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"}";
        JsonNode verificationRelationshipEventJsonNode = new ObjectMapper().readTree(jsonString);

        HcsDidUpdateVerificationRelationshipEvent eventFromJson = HcsDidUpdateVerificationRelationshipEvent.fromJsonTree(verificationRelationshipEventJsonNode);


        String expectedJsonString = "{\"VerificationRelationship\":{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#key-1\",\"relationshipType\":\"authentication\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"}}";
        JsonNode expected = new ObjectMapper().readTree(expectedJsonString);

        assertEquals(expected, eventFromJson.toJsonTree());
        assertEquals(expectedJsonString, eventFromJson.toJSON());
    }


}
