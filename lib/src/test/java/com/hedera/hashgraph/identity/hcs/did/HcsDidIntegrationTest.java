package com.hedera.hashgraph.identity.hcs.did;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.event.service.ServiceType;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HcsDidIntegrationTest {

    final AccountId operatorId = AccountId.fromString("0.0.28520500");
    final PrivateKey operatorKey = PrivateKey.fromString("302e020100300506032b65700422042024a2b2bfffa54e492e06f4b092bf9701fa0f230223754bf0b4c350eed222a3b0");
    final List<String> mirrorNetworks = List.of("hcs.testnet.mirrornode.hedera.com:5600");

    final Client client = Client.forTestnet();

    private final PrivateKey registeredDidPrivateKey = PrivateKey.generateED25519();
    private final HcsDid registeredDid;

    HcsDidIntegrationTest() throws DidError, ReceiptStatusException, JsonProcessingException, PrecheckStatusException, TimeoutException {
        client.setOperator(operatorId, operatorKey);

        // Pre-creating one DID for further tests
        // TODO: consider using @BeforeAll or something similar
        this.registeredDid = new HcsDid(null, this.registeredDidPrivateKey, this.client);
        this.registeredDid.register();
    }

    /**
     * #register
     */

    @Test
    @DisplayName("throws error if DID is already registered")
    void itThrowsDidAlreadyRegisteredError() throws DidError {
        HcsDid did = new HcsDid(this.registeredDid.getIdentifier(), this.registeredDidPrivateKey, this.client);
        Exception exception = assertThrows(DidError.class, () -> {
            did.register();
        });
        assertEquals("DID is already registered", exception.getMessage());
    }

    @Test
    @DisplayName("throws error if client configuration is missing")
    void itThrowsClientConfigurationMissingError() throws DidError {
        HcsDid did = new HcsDid(null, this.registeredDidPrivateKey, null);
        Exception exception = assertThrows(DidError.class, () -> {
            did.register();
        });
        assertEquals("Client configuration is missing", exception.getMessage());
    }

    @Test
    @DisplayName("creates new DID by registering a topic and submitting first message")
    void itRegistersNewDid() throws ReceiptStatusException, JsonProcessingException, PrecheckStatusException, TimeoutException, DidError {
        HcsDid did = new HcsDid(null, this.registeredDidPrivateKey, this.client);

        assertNull(did.getIdentifier());
        did.register();
        assertNotNull(did.getIdentifier());
    }

    /**
     * #resolve
     */

    @Test
    @DisplayName("throws error about unregistered DID")
    void itThrowsNotRegisteredDidError() throws DidError {
        HcsDid did = new HcsDid(null, this.registeredDidPrivateKey, this.client);
        Exception exception = assertThrows(DidError.class, () -> {
            did.resolve();
        });
        assertEquals("DID is not registered", exception.getMessage());
    }

    @Test
    @DisplayName("throws error about missing Client parameter")
    void itThrowsMissingClientConfigurationError() throws DidError {
        HcsDid did = new HcsDid("did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.29613327", this.registeredDidPrivateKey, null);
        Exception exception = assertThrows(DidError.class, () -> {
            did.resolve();
        });
        assertEquals("Client configuration is missing", exception.getMessage());
    }

    @Test
    @DisplayName("successfully resolves just registered DID")
    void itResolvesDid() throws JsonProcessingException, DidError {
        String json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + this.registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + this.registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(this.registeredDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + this.registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + this.registeredDid.getIdentifier() + "#did-root-key\"]}", json);
    }

    /**
     * Add service meta-information
     */

    @Test
    @DisplayName("throws error if privatekey is missing")
    void itAddServiceThrowsMissingPrivateKeyError() throws DidError {
        HcsDid did = new HcsDid(this.registeredDid.getIdentifier(), null, null);

        Exception exception = assertThrows(DidError.class, () -> {
            did.addService(null, null, null);
        });
        assertEquals("privateKey is missing", exception.getMessage());
    }

    @Test
    @DisplayName("throws error if client configuration is missing")
    void itAddServiceThrowsMissingClientConfigurationError() throws DidError {
        HcsDid did = new HcsDid(null, this.registeredDidPrivateKey, null);

        Exception exception = assertThrows(DidError.class, () -> {
            did.addService(null, null, null);
        });
        assertEquals("Client configuration is missing", exception.getMessage());
    }

    @Test
    @DisplayName("throws error if Service arguments are missing")
    void itAddServiceThrowsArgumentMissingError() throws DidError {
        HcsDid did = new HcsDid(null, this.registeredDidPrivateKey, this.client);

        Exception exception = assertThrows(DidError.class, () -> {
            did.addService(null, null, null);
        });
        assertEquals("Validation failed. Services args are missing", exception.getMessage());
    }

    @Test
    @DisplayName("throws error if event id is not valid")
    void itAddServiceThrowsInvalidIdError() throws DidError {
        Exception exception = assertThrows(DidError.class, () -> {
            this.registeredDid.addService(
                    this.registeredDid.getIdentifier() + "#invalid-1",
                    ServiceType.LINKED_DOMAINS,
                    "https://example.com/vcs"
            );
        });
        assertEquals("Event ID is invalid. Expected format: {did}#service-{integer}", exception.getMessage());
    }

    @Test
    @DisplayName("publish a new Service message and verify DID Document")
    void itAddsNewServiceToTheDocument() throws DidError, JsonProcessingException, InterruptedException {
        this.registeredDid.addService(
                this.registeredDid.getIdentifier() + "#service-1",
                ServiceType.LINKED_DOMAINS,
                "https://example.com/vcs"
        );

        Thread.sleep(3000);

        String json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + this.registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + this.registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(this.registeredDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + this.registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + this.registeredDid.getIdentifier() + "#did-root-key\"]}", json);
    }

    /**
     * #changeOwner
     * TODO: later
     */

    /**
     * #delete
     */

    @Test
    @DisplayName("throws error if DID is not registered")
    void itThrowsDeleteDidNotRegisteredError() throws DidError {
        HcsDid did = new HcsDid(null, this.registeredDidPrivateKey, this.client);
        Exception exception = assertThrows(DidError.class, () -> {
            did.delete();
        });
        assertEquals("DID is not registered", exception.getMessage());
    }

    @Test
    @DisplayName("throws error if instance has no privateKey assigned")
    void itThrowsDeleteMissingPrivateKeyError() throws DidError {
        HcsDid did = new HcsDid(this.registeredDid.getIdentifier(), null, this.client);
        Exception exception = assertThrows(DidError.class, () -> {
            did.delete();
        });
        assertEquals("privateKey is missing", exception.getMessage());
    }

    @Test
    @DisplayName("throws error if instance has no client assigned")
    void itThrowsDeleteMissingClientConfigurationError() throws DidError {
        HcsDid did = new HcsDid(this.registeredDid.getIdentifier(), this.registeredDidPrivateKey, null);
        Exception exception = assertThrows(DidError.class, () -> {
            did.delete();
        });
        assertEquals("Client configuration is missing", exception.getMessage());
    }

    @Test
    @DisplayName("deletes the DID document")
    void itSuccessfulyDeletesDid() throws DidError, JsonProcessingException, InterruptedException {
        String json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + this.registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + this.registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(this.registeredDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + this.registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + this.registeredDid.getIdentifier() + "#did-root-key\"]}", json);
        registeredDid.delete();
        Thread.sleep(3000);
        String jsonAfter = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[],\"assertionMethod\":[],\"authentication\":[]}", jsonAfter);
    }

}
