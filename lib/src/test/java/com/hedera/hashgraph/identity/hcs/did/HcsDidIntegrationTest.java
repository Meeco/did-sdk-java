package com.hedera.hashgraph.identity.hcs.did;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.did.event.service.ServiceType;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.VerificationMethodSupportedKeyType;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.VerificationRelationshipSupportedKeyType;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.VerificationRelationshipType;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HcsDidIntegrationTest {


    private static final Client client = Client.forTestnet();
    private static final PrivateKey registeredDidPrivateKey = PrivateKey.generateED25519();
    private static final String CONFIG_FILE = "config.properties";
    private static HcsDid registeredDid;

    @BeforeAll
    public static void setup() throws DidError, ReceiptStatusException, PrecheckStatusException, TimeoutException {

        try (InputStream input = HcsDidIntegrationTest.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {

            Properties prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }

            //load a properties file from class path, inside static method
            prop.load(input);

            AccountId operatorId = AccountId.fromString(prop.getProperty("OPERATOR_ID"));
            PrivateKey operatorKey = PrivateKey.fromString(prop.getProperty("OPERATOR_KEY"));
            client.setOperator(operatorId, operatorKey);

            registeredDid = new HcsDid(null, registeredDidPrivateKey, client);
            registeredDid.register();


        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }


    /**
     * #register
     */

    @Test
    @Order(111)
    @DisplayName("throws error if DID is already registered")
    void itThrowsDidAlreadyRegisteredError() throws DidError {
        HcsDid did = new HcsDid(registeredDid.getIdentifier(), registeredDidPrivateKey, client);
        Exception exception = assertThrows(DidError.class, did::register);
        assertEquals("DID is already registered", exception.getMessage());
    }

    @Test
    @Order(112)
    @DisplayName("throws error if client configuration is missing")
    void itThrowsClientConfigurationMissingError() throws DidError {
        HcsDid did = new HcsDid(null, registeredDidPrivateKey, null);
        Exception exception = assertThrows(DidError.class, did::register);
        assertEquals("Client configuration is missing", exception.getMessage());
    }

    @Test
    @Order(113)
    @DisplayName("creates new DID by registering a topic and submitting first message")
    void itRegistersNewDid() throws ReceiptStatusException, JsonProcessingException, PrecheckStatusException, TimeoutException, DidError {
        HcsDid did = new HcsDid(null, registeredDidPrivateKey, client);

        assertNull(did.getIdentifier());
        did.register();
        assertNotNull(did.getIdentifier());
    }

    /**
     * #resolve
     */

    @Test
    @Order(211)
    @DisplayName("throws error about unregistered DID")
    void itThrowsNotRegisteredDidError() throws DidError {
        HcsDid did = new HcsDid(null, registeredDidPrivateKey, client);
        Exception exception = assertThrows(DidError.class, did::resolve);
        assertEquals("DID is not registered", exception.getMessage());
    }

    @Test
    @Order(212)
    @DisplayName("throws error about missing Client parameter")
    void itThrowsMissingClientConfigurationError() throws DidError {
        HcsDid did = new HcsDid("did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.29613327", registeredDidPrivateKey, null);
        Exception exception = assertThrows(DidError.class, did::resolve);
        assertEquals("Client configuration is missing", exception.getMessage());
    }

    @Test
    @Order(213)
    @DisplayName("successfully resolves just registered DID")
    void itResolvesDid() throws JsonProcessingException, DidError {
        String json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"]}", json);
    }

    /**
     * DID service meta-information
     */

    @Test
    @Order(311)
    @DisplayName("throws error if privatekey is missing")
    void itAddServiceThrowsMissingPrivateKeyError() throws DidError {
        HcsDid did = new HcsDid(registeredDid.getIdentifier(), null, null);

        Exception exception = assertThrows(DidError.class, () -> did.addService(null, null, null));
        assertEquals("privateKey is missing", exception.getMessage());
    }

    @Test
    @Order(312)
    @DisplayName("throws error if client configuration is missing")
    void itAddServiceThrowsMissingClientConfigurationError() throws DidError {
        HcsDid did = new HcsDid(null, registeredDidPrivateKey, null);

        Exception exception = assertThrows(DidError.class, () -> did.addService(null, null, null));
        assertEquals("Client configuration is missing", exception.getMessage());
    }

    @Test
    @Order(313)
    @DisplayName("throws error if Service arguments are missing")
    void itAddServiceThrowsArgumentMissingError() throws DidError {
        HcsDid did = new HcsDid(null, registeredDidPrivateKey, client);

        Exception exception = assertThrows(DidError.class, () -> did.addService(null, null, null));
        assertEquals("Validation failed. Services args are missing", exception.getMessage());
    }

    @Test
    @Order(314)
    @DisplayName("throws error if event id is not valid")
    void itAddServiceThrowsInvalidIdError() {
        Exception exception = assertThrows(DidError.class, () -> registeredDid.addService(registeredDid.getIdentifier() + "#invalid-1", ServiceType.LINKED_DOMAINS, "https://example.com/vcs"));
        assertEquals("Event ID is invalid. Expected format: {did}#service-{integer}", exception.getMessage());
    }

    @Test
    @Order(315)
    @DisplayName("publish, update and revoke a new Service message and verify DID Document")
    void itAddsUpdatesAndRevokesAServiceToTheDocument() throws DidError, JsonProcessingException {
        // Publish
        registeredDid.addService(registeredDid.getIdentifier() + "#service-1", ServiceType.LINKED_DOMAINS, "https://example.com/vcs");
        String json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"service\":[{\"id\":\"" + registeredDid.getIdentifier() + "#service-1\",\"type\":\"LinkedDomains\",\"serviceEndpoint\":\"https://example.com/vcs\"}]}", json);

        // Update
        registeredDid.updateService(registeredDid.getIdentifier() + "#service-1", ServiceType.DID_COMM_MESSAGING, "https://example.com/msg");
        json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"service\":[{\"id\":\"" + registeredDid.getIdentifier() + "#service-1\",\"type\":\"DIDCommMessaging\",\"serviceEndpoint\":\"https://example.com/msg\"}]}", json);

        // Revoke
        registeredDid.revokeService(registeredDid.getIdentifier() + "#service-1");
        json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"]}", json);
    }

    /**
     * DID Verification Method meta-information
     */

    @Test
    @Order(411)
    @DisplayName("throws error if privatekey is missing")
    void itVerificationMethodThrowsMissingPrivateKeyError() throws DidError {
        HcsDid did = new HcsDid(registeredDid.getIdentifier(), null, null);
        Exception exception = assertThrows(DidError.class, () -> did.addVerificationMethod(null, null, null, null));
        assertEquals("privateKey is missing", exception.getMessage());
    }

    @Test
    @Order(412)
    @DisplayName("throws error if client configuration is missing")
    void itVerificationMethodThrowsMissingClientConfigurationError() throws DidError {
        HcsDid did = new HcsDid(null, registeredDidPrivateKey, null);
        Exception exception = assertThrows(DidError.class, () -> did.addVerificationMethod(null, null, null, null));
        assertEquals("Client configuration is missing", exception.getMessage());
    }

    @Test
    @Order(413)
    @DisplayName("throws error if Verification Method arguments are missing")
    void itVerificationMethodThrowsMissingArgumentsError() throws DidError {
        HcsDid did = new HcsDid(null, registeredDidPrivateKey, client);
        Exception exception = assertThrows(DidError.class, () -> did.addVerificationMethod(null, null, null, null));
        assertEquals("Validation failed. Verification Method args are missing", exception.getMessage());
    }

    @Test
    @Order(414)
    @DisplayName("publish, update and revoke a new VerificationMethod message and verify DID Document")
    void itAddsUpdatesAndRevokesVerificationMethodToTheDocument() throws DidError, JsonProcessingException {
        PrivateKey newKey1 = PrivateKey.generateED25519();
        PrivateKey newKey2 = PrivateKey.generateED25519();

        // Publish
        registeredDid.addVerificationMethod(registeredDid.getIdentifier() + "#key-123", VerificationMethodSupportedKeyType.ED25519_VERIFICATION_KEY_2018, registeredDid.getIdentifier(), newKey1.getPublicKey());
        String json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"},{\"id\":\"" + registeredDid.getIdentifier() + "#key-123\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(newKey1.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"]}", json);

        // Update
        registeredDid.updateVerificationMethod(registeredDid.getIdentifier() + "#key-123", VerificationMethodSupportedKeyType.ED25519_VERIFICATION_KEY_2018, registeredDid.getIdentifier(), newKey2.getPublicKey());
        json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"},{\"id\":\"" + registeredDid.getIdentifier() + "#key-123\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(newKey2.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"]}", json);

        // Revoke
        registeredDid.revokeVerificationMethod(registeredDid.getIdentifier() + "#key-123");
        json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"]}", json);
    }

    /**
     * DID Verification Relationship meta-information
     */

    @Test
    @Order(511)
    @DisplayName("throws error if privatekey is missing")
    void itVerificationRelationshipThrowsPrivateKeyMissingError() throws DidError {
        HcsDid did = new HcsDid(registeredDid.getIdentifier(), null, null);
        Exception exception = assertThrows(DidError.class, () -> did.addVerificationRelationship(null, null, null, null, null));
        assertEquals("privateKey is missing", exception.getMessage());
    }

    @Test
    @Order(512)
    @DisplayName("throws error if client configuration is missing")
    void itVerificationRelationshipThrowsConfigurationMissingError() throws DidError {
        HcsDid did = new HcsDid(null, registeredDidPrivateKey, null);
        Exception exception = assertThrows(DidError.class, () -> did.addVerificationRelationship(null, null, null, null, null));
        assertEquals("Client configuration is missing", exception.getMessage());
    }

    @Test
    @Order(513)
    @DisplayName("throws error if Verification Relationship arguments are missing")
    void itVerificationRelationshipThrowsMissingArgumentsError() throws DidError {
        HcsDid did = new HcsDid(null, registeredDidPrivateKey, client);
        Exception exception = assertThrows(DidError.class, () -> did.addVerificationRelationship(null, null, null, null, null));
        assertEquals("Validation failed. Verification Relationship args are missing", exception.getMessage());
    }

    @Test
    @Order(514)
    @DisplayName("publish, update and revoke a new VerificationRelationship message and verify DID Document")
    void itAddsUpdatesAndRevokesAVerificationRelationshipToDidDocument() throws DidError, JsonProcessingException {
        PrivateKey newKey1 = PrivateKey.generateED25519();
        PrivateKey newKey2 = PrivateKey.generateED25519();

        // Publish
        registeredDid.addVerificationRelationship(registeredDid.getIdentifier() + "#key-321", VerificationRelationshipType.CAPABILITY_DELEGATION, VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018, registeredDid.getIdentifier(), newKey1.getPublicKey());
        String json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"},{\"id\":\"" + registeredDid.getIdentifier() + "#key-321\",\"relationshipType\":\"capabilityDelegation\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(newKey1.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"capabilityDelegation\":[\"" + registeredDid.getIdentifier() + "#key-321\"]}", json);

        // Update
        registeredDid.updateVerificationRelationship(registeredDid.getIdentifier() + "#key-321", VerificationRelationshipType.CAPABILITY_DELEGATION, VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018, registeredDid.getIdentifier(), newKey2.getPublicKey());
        json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"},{\"id\":\"" + registeredDid.getIdentifier() + "#key-321\",\"relationshipType\":\"capabilityDelegation\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(newKey2.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"capabilityDelegation\":[\"" + registeredDid.getIdentifier() + "#key-321\"]}", json);

        // Revoke
        registeredDid.revokeVerificationRelationship(registeredDid.getIdentifier() + "#key-321", VerificationRelationshipType.CAPABILITY_DELEGATION);
        json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"]}", json);
    }

    /**
     * #changeOwner
     */

    @Test
    @Order(611)
    @DisplayName("throws error that DID is not registered")
    void itChangeOwnerThrowsDIDNotRegisteredError() throws DidError {
        String newOwnerIdentifier = "did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.29613327";
        HcsDid did = new HcsDid(null, registeredDidPrivateKey, client);

        Exception exception = assertThrows(DidError.class, () -> did.changeOwner(newOwnerIdentifier, PrivateKey.generateED25519()));
        assertEquals("DID is not registered", exception.getMessage());
    }

    @Test
    @Order(612)
    @DisplayName("throws error that privateKey is missing")
    void itChangeOwnerThrowsPrivateKeyMissingError() throws DidError {
        String docIdentifier = "did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.99999999";
        String newOwnerIdentifier = "did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.29613327";
        HcsDid did = new HcsDid(docIdentifier, null, client);

        Exception exception = assertThrows(DidError.class, () -> did.changeOwner(newOwnerIdentifier, PrivateKey.generateED25519()));
        assertEquals("privateKey is missing", exception.getMessage());
    }

    @Test
    @Order(613)
    @DisplayName("throws error that Client configuration is missing")
    void itChangeOwnerThrowsClientConfigurationMissingError() throws DidError {
        String docIdentifier = "did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.99999999";
        String newOwnerIdentifier = "did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.29613327";
        HcsDid did = new HcsDid(docIdentifier, registeredDidPrivateKey, null);

        Exception exception = assertThrows(DidError.class, () -> did.changeOwner(newOwnerIdentifier, PrivateKey.generateED25519()));
        assertEquals("Client configuration is missing", exception.getMessage());
    }

    @Test
    @Order(614)
    @DisplayName("throws error thet newPrivateKey is missing")
    void itChangeOwnerThrowsNewPrivateKeyMissingError() throws DidError {
        String docIdentifier = "did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.99999999";
        String newOwnerIdentifier = "did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.29613327";
        HcsDid did = new HcsDid(docIdentifier, registeredDidPrivateKey, client);

        Exception exception = assertThrows(DidError.class, () -> did.changeOwner(newOwnerIdentifier, null));
        assertEquals("newPrivateKey is missing", exception.getMessage());
    }

    @Test
    @Order(615)
    @DisplayName("changes the owner of the document")
    void itChangesTheOwnerOfTheDocument() throws ReceiptStatusException, DidError, JsonProcessingException, PrecheckStatusException, TimeoutException {
        PrivateKey newDidPrivateKey = PrivateKey.generateED25519();
        String newOwnerIdentifier = "did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.29613327";

        // Change to a new owner
        registeredDid.changeOwner(newOwnerIdentifier, newDidPrivateKey);
        String json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"controller\":\"" + newOwnerIdentifier + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + newOwnerIdentifier + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(newDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"]}", json);

        // Change back
        HcsDid did = new HcsDid(registeredDid.getIdentifier(), newDidPrivateKey, client);
        did.changeOwner(registeredDid.getIdentifier(), registeredDidPrivateKey);
        json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"]}", json);
    }

    /**
     * #delete
     */

    @Test
    @Order(711)
    @DisplayName("throws error if DID is not registered")
    void itThrowsDeleteDidNotRegisteredError() throws DidError {
        HcsDid did = new HcsDid(null, registeredDidPrivateKey, client);
        Exception exception = assertThrows(DidError.class, did::delete);
        assertEquals("DID is not registered", exception.getMessage());
    }

    @Test
    @Order(712)
    @DisplayName("throws error if instance has no privateKey assigned")
    void itThrowsDeleteMissingPrivateKeyError() throws DidError {
        HcsDid did = new HcsDid(registeredDid.getIdentifier(), null, client);
        Exception exception = assertThrows(DidError.class, did::delete);
        assertEquals("privateKey is missing", exception.getMessage());
    }

    @Test
    @Order(713)
    @DisplayName("throws error if instance has no client assigned")
    void itThrowsDeleteMissingClientConfigurationError() throws DidError {
        HcsDid did = new HcsDid(registeredDid.getIdentifier(), registeredDidPrivateKey, null);
        Exception exception = assertThrows(DidError.class, did::delete);
        assertEquals("Client configuration is missing", exception.getMessage());
    }

    @Test
    @Order(714)
    @DisplayName("deletes the DID document")
    void itSuccessfulyDeletesDid() throws DidError, JsonProcessingException {
        String json = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[{\"id\":\"" + registeredDid.getIdentifier() + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + registeredDid.getIdentifier() + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(registeredDidPrivateKey.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"],\"authentication\":[\"" + registeredDid.getIdentifier() + "#did-root-key\"]}", json);
        registeredDid.delete();
        String jsonAfter = registeredDid.resolve().toJsonTree().toString();
        assertEquals("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + registeredDid.getIdentifier() + "\",\"verificationMethod\":[],\"assertionMethod\":[],\"authentication\":[]}", jsonAfter);
    }

}
