package com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HcsDidRevokeVerificationRelationshipEventTest {


    PrivateKey privateKey = PrivateKey.fromString(
            "302e020100300506032b6570042204209044d8f201e4b0aa7ba8ed577b0334b8cb6e38aad6c596171b5b1246737f5079"
    );
    String identifier = "did:hedera:testnet:" + Hashing.Multibase.encode(privateKey.getPublicKey().toBytes()) + "_0.0.29613327";
    HcsDidRevokeVerificationRelationshipEvent event = new HcsDidRevokeVerificationRelationshipEvent(
            identifier + "#key-1",
            VerificationRelationshipType.AUTHENTICATION
    );

    public HcsDidRevokeVerificationRelationshipEventTest() throws DidError {
    }

    @Test
    void itTargetsService() {
        assertEquals(event.getTargetName(), HcsDidEventTargetName.VERIFICATION_RELATIONSHIP);
    }

    @Test
    void itThrowsErrorIfIdIsNULL() {
        try {
            new HcsDidRevokeVerificationRelationshipEvent(
                    null,
                    VerificationRelationshipType.AUTHENTICATION
            );
        } catch (DidError error) {
            assertEquals("Validation failed. Verification Relationship args are missing", error.getMessage());
        }
    }

    @Test
    void itThrowsErrorIfRelationshipTypeIsNULL() {
        try {
            new HcsDidRevokeVerificationRelationshipEvent(
                    identifier + "#key-1",
                    null
            );
        } catch (DidError error) {
            assertEquals("Validation failed. Verification Relationship args are missing", error.getMessage());
        }
    }

    @Test
    void itThrowsErrorIfIdIsNotValid() {
        try {
            new HcsDidRevokeVerificationRelationshipEvent(
                    identifier,
                    VerificationRelationshipType.AUTHENTICATION
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
    void getBase64() {
        assertEquals(
                "eyJWZXJpZmljYXRpb25SZWxhdGlvbnNoaXAiOnsiaWQiOiJkaWQ6aGVkZXJhOnRlc3RuZXQ6ejZNa29nVnpvR0pNVlZMaGF6ODJjQTVqWlFLQUFxVWdoaENycHprU0RGRHd4ZkphXzAuMC4yOTYxMzMyNyNrZXktMSIsInJlbGF0aW9uc2hpcFR5cGUiOiJhdXRoZW50aWNhdGlvbiJ9fQ==",
                event.getBase64()
        );
    }

    @Test
    void toJsonTree() throws JsonProcessingException {
        String jsonString = "{\"VerificationRelationship\":{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#key-1\",\"relationshipType\":\"authentication\"}}";
        JsonNode verificationRelationshipRevokeEventJsonNode = new ObjectMapper().readTree(jsonString);

        assertEquals(verificationRelationshipRevokeEventJsonNode, event.toJsonTree());
    }

    @Test
    void toJSON() {
        assertEquals("{\"VerificationRelationship\":{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#key-1\",\"relationshipType\":\"authentication\"}}",
                event.toJSON());
    }


    @Test
    void fromJsonTree() throws JsonProcessingException, DidError {
        String jsonString = "{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#key-1\",\"relationshipType\":\"authentication\"}";

        JsonNode verificationRelationshipRevokeEventJsonNode = new ObjectMapper().readTree(jsonString);

        HcsDidRevokeVerificationRelationshipEvent eventFromJson = HcsDidRevokeVerificationRelationshipEvent.fromJsonTree(verificationRelationshipRevokeEventJsonNode);


        String expectedJsonString = "{\"VerificationRelationship\":{\"id\":\"did:hedera:testnet:z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa_0.0.29613327#key-1\",\"relationshipType\":\"authentication\"}}";
        JsonNode expected = new ObjectMapper().readTree(expectedJsonString);

        assertEquals(expected, eventFromJson.toJsonTree());
        assertEquals(expectedJsonString, eventFromJson.toJSON());
    }

}
