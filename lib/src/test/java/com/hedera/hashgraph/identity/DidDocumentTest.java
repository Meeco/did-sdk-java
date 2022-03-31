package com.hedera.hashgraph.identity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.identity.hcs.did.HcsDidMessage;
import com.hedera.hashgraph.identity.hcs.did.event.document.HcsDidDeleteEvent;
import com.hedera.hashgraph.identity.hcs.did.event.owner.HcsDidCreateDidOwnerEvent;
import com.hedera.hashgraph.identity.hcs.did.event.owner.HcsDidUpdateDidOwnerEvent;
import com.hedera.hashgraph.identity.hcs.did.event.service.HcsDidCreateServiceEvent;
import com.hedera.hashgraph.identity.hcs.did.event.service.HcsDidRevokeServiceEvent;
import com.hedera.hashgraph.identity.hcs.did.event.service.HcsDidUpdateServiceEvent;
import com.hedera.hashgraph.identity.hcs.did.event.service.ServiceType;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.HcsDidCreateVerificationMethodEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.HcsDidRevokeVerificationMethodEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.HcsDidUpdateVerificationMethodEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.VerificationMethodSupportedKeyType;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.*;
import com.hedera.hashgraph.identity.utils.Hashing;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DidDocumentTest {

    PrivateKey privateKey = PrivateKey.fromString(
            "302e020100300506032b6570042204209044d8f201e4b0aa7ba8ed577b0334b8cb6e38aad6c596171b5b1246737f5079"
    );
    String identifier = String.format("did:hedera:testnet:%s_0.0.29613327", Hashing.Multibase.encode(privateKey.getPublicKey().toBytes()));

    @Test
    @DisplayName("returns empty document if not events were passed")
    void itTestEmptyDidDocument() throws JsonProcessingException {

        HcsDidMessage[] HcsDidMessage = {};
        DidDocument doc = new DidDocument(identifier, HcsDidMessage);
        JsonNode expectedResult = new ObjectMapper().readTree("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + identifier + "\",\"verificationMethod\":[],\"assertionMethod\":[],\"authentication\":[]}");

        assertEquals(expectedResult, doc.toJsonTree());

        assertNull(doc.getCreated());
        assertNull(doc.getUpdated());
        assertFalse(doc.getDeactivated());
        assertNull(doc.getVersionId());

    }

    @Test
    @DisplayName("ignores events til first create DIDOwner event")
    void itIgnoreEventsTilFirstCreateDidOwnerEvent() throws DidError, JsonProcessingException {

        HcsDidMessage[] HcsDidMessage = {
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateServiceEvent(
                                identifier + "#service-1",
                                ServiceType.LINKED_DOMAINS,
                                "https://test.identity.com"
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateDidOwnerEvent(identifier + "#did-root-key", identifier, privateKey.getPublicKey())
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateServiceEvent(
                                identifier + "#service-2",
                                ServiceType.LINKED_DOMAINS,
                                "https://test2.identity.com"
                        )
                ),
        };
        DidDocument doc = new DidDocument(identifier, HcsDidMessage);

        JsonNode expectedResult = new ObjectMapper().readTree("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + identifier + "\",\"verificationMethod\":[{\"id\":\"" + identifier + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"}],\"assertionMethod\":[\"" + identifier + "#did-root-key\"],\"authentication\":[\"" + identifier + "#did-root-key\"],\"service\":[{\"id\":\"" + identifier + "#service-2\",\"type\":\"LinkedDomains\",\"serviceEndpoint\":\"https://test2.identity.com\"}]}");

        assertEquals(expectedResult, doc.toJsonTree());


        assertNotNull(doc.getCreated());
        assertNotNull(doc.getUpdated());
        assertFalse(doc.getDeactivated());
        assertNotNull(doc.getVersionId());

    }

    @Test
    @DisplayName("handles create DIDOwner event")
    void itTestCreateDidDocumentFromDidOwnerEvent() throws DidError, JsonProcessingException {
        HcsDidMessage[] HcsDidMessage = {
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateDidOwnerEvent(identifier + "#did-root-key", identifier, privateKey.getPublicKey())
                ),

        };
        DidDocument doc = new DidDocument(identifier, HcsDidMessage);

        JsonNode expectedResult = new ObjectMapper().readTree("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + identifier + "\",\"verificationMethod\":[{\"id\":\"" + identifier + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"}],\"assertionMethod\":[\"" + identifier + "#did-root-key\"],\"authentication\":[\"" + identifier + "#did-root-key\"]}");
        assertEquals(expectedResult, doc.toJsonTree());

        assertNotNull(doc.getCreated());
        assertNotNull(doc.getUpdated());
        assertFalse(doc.getDeactivated());
        assertNotNull(doc.getVersionId());

    }

    @Test
    @DisplayName("handles DID delete event")
    void itTestDidDeleteEvent() throws DidError, JsonProcessingException {
        HcsDidMessage[] HcsDidMessage = {
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateDidOwnerEvent(identifier + "#did-root-key", identifier, privateKey.getPublicKey())
                ),
                new HcsDidMessage(DidMethodOperation.DELETE, identifier, new HcsDidDeleteEvent()),
        };

        DidDocument doc = new DidDocument(identifier, HcsDidMessage);

        JsonNode expectedResult = new ObjectMapper().readTree("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + identifier + "\",\"verificationMethod\":[],\"assertionMethod\":[],\"authentication\":[]}");
        assertEquals(expectedResult, doc.toJsonTree());

        assertNull(doc.getCreated());
        assertNull(doc.getUpdated());
        assertTrue(doc.getDeactivated());
        assertNull(doc.getVersionId());

    }

    @Test
    @DisplayName("handles change DID owner event")
    void itTestChangeDidOwnerEvent() throws DidError, JsonProcessingException {

        PrivateKey otherOwnerKey = PrivateKey.generateED25519();
        String otherOwnerIdentifier = String.format("did:hedera:testnet:%s_0.0.29999999", Hashing.Multibase.encode(otherOwnerKey.getPublicKey().toBytes()));
        PrivateKey key2 = PrivateKey.generateED25519();


        HcsDidMessage[] HcsDidMessage = {
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateDidOwnerEvent(identifier + "#did-root-key", identifier, privateKey.getPublicKey())
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateVerificationRelationshipEvent(
                                identifier + "#key-2",
                                VerificationRelationshipType.CAPABILITY_DELEGATION,
                                VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                                identifier,
                                key2.getPublicKey()
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.UPDATE,
                        identifier,
                        new HcsDidUpdateDidOwnerEvent(
                                otherOwnerIdentifier + "#did-root-key",
                                otherOwnerIdentifier,
                                otherOwnerKey.getPublicKey()
                        )
                )
        };

        DidDocument doc = new DidDocument(identifier, HcsDidMessage);


        JsonNode expectedResult = new ObjectMapper().readTree("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + identifier + "\",\"controller\":\"" + otherOwnerIdentifier + "\",\"verificationMethod\":[{\"id\":\"" + otherOwnerIdentifier + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + otherOwnerIdentifier + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(otherOwnerKey.getPublicKey().toBytes()) + "\"},{\"id\":\"" + identifier + "#key-2\",\"relationshipType\":\"capabilityDelegation\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(key2.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + otherOwnerIdentifier + "#did-root-key\"],\"authentication\":[\"" + otherOwnerIdentifier + "#did-root-key\"],\"capabilityDelegation\":[\"" + identifier + "#key-2\"]}");
        assertEquals(expectedResult, doc.toJsonTree());

        assertNotNull(doc.getCreated());
        assertNotNull(doc.getUpdated());
        assertFalse(doc.getDeactivated());
        assertNotNull(doc.getVersionId());

    }

    @Test
    @DisplayName("successfully handles add service, verificationMethod and verificationRelationship events")
    void itTestAddServiceVerificationMethodAndRelationshipEvents() throws DidError, JsonProcessingException {

        PrivateKey key1 = PrivateKey.generateED25519();
        PrivateKey key2 = PrivateKey.generateED25519();

        HcsDidMessage[] HcsDidMessage = {
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateDidOwnerEvent(identifier + "#did-root-key", identifier, privateKey.getPublicKey())
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateServiceEvent(
                                identifier + "#service-1",
                                ServiceType.LINKED_DOMAINS,
                                "https://test.identity.com"
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateVerificationMethodEvent(
                                identifier + "#key-1",
                                VerificationMethodSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                                identifier,
                                key1.getPublicKey()
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateVerificationRelationshipEvent(
                                identifier + "#key-2",
                                VerificationRelationshipType.CAPABILITY_DELEGATION,
                                VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                                identifier,
                                key2.getPublicKey()
                        )
                )
        };

        DidDocument doc = new DidDocument(identifier, HcsDidMessage);

        JsonNode expectedResult = new ObjectMapper().readTree("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + identifier + "\",\"verificationMethod\":[{\"id\":\"" + identifier + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"},{\"id\":\"" + identifier + "#key-1\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(key1.getPublicKey().toBytes()) + "\"},{\"id\":\"" + identifier + "#key-2\",\"relationshipType\":\"capabilityDelegation\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(key2.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + identifier + "#did-root-key\"],\"authentication\":[\"" + identifier + "#did-root-key\"],\"capabilityDelegation\":[\"" + identifier + "#key-2\"],\"service\":[{\"id\":\"" + identifier + "#service-1\",\"type\":\"LinkedDomains\",\"serviceEndpoint\":\"https://test.identity.com\"}]}");
        assertEquals(expectedResult, doc.toJsonTree());

        assertNotNull(doc.getCreated());
        assertNotNull(doc.getUpdated());
        assertFalse(doc.getDeactivated());
        assertNotNull(doc.getVersionId());

    }


    @Test
    @DisplayName("successfully handles update service, verificationMethod and verificationRelationship events")
    void itTestUpdateServiceVerificationMethodAndRelationshipEvents() throws DidError, JsonProcessingException {

        PrivateKey key1 = PrivateKey.generateED25519();
        PrivateKey key2 = PrivateKey.generateED25519();
        PrivateKey key3 = PrivateKey.generateED25519();
        PrivateKey key4 = PrivateKey.generateED25519();
        PrivateKey key5 = PrivateKey.generateED25519();


        HcsDidMessage[] HcsDidMessage = {
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateDidOwnerEvent(identifier + "#did-root-key", identifier, privateKey.getPublicKey())
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateServiceEvent(
                                identifier + "#service-1",
                                ServiceType.LINKED_DOMAINS,
                                "https://test.identity.com"
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateServiceEvent(
                                identifier + "#service-2",
                                ServiceType.LINKED_DOMAINS,
                                "https://test2.identity.com"
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateVerificationMethodEvent(
                                identifier + "#key-1",
                                VerificationMethodSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                                identifier,
                                key1.getPublicKey()
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateVerificationRelationshipEvent(
                                identifier + "#key-2",
                                VerificationRelationshipType.CAPABILITY_DELEGATION,
                                VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                                identifier,
                                key2.getPublicKey()
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateVerificationRelationshipEvent(
                                identifier + "#key-3",
                                VerificationRelationshipType.AUTHENTICATION,
                                VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                                identifier,
                                key3.getPublicKey()
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.UPDATE,
                        identifier,
                        new HcsDidUpdateServiceEvent(
                                identifier + "#service-1",
                                ServiceType.LINKED_DOMAINS,
                                "https://new.test.identity.com"
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.UPDATE,
                        identifier,
                        new HcsDidUpdateVerificationMethodEvent(
                                identifier + "#key-1",
                                VerificationMethodSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                                identifier,
                                key4.getPublicKey()
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.UPDATE,
                        identifier,
                        new HcsDidUpdateVerificationRelationshipEvent(
                                identifier + "#key-2",
                                VerificationRelationshipType.CAPABILITY_DELEGATION,
                                VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                                identifier,
                                key5.getPublicKey()
                        )
                )
        };

        DidDocument doc = new DidDocument(identifier, HcsDidMessage);

        JsonNode expectedResult = new ObjectMapper().readTree("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + identifier + "\",\"verificationMethod\":[{\"id\":\"" + identifier + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"},{\"id\":\"" + identifier + "#key-1\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(key4.getPublicKey().toBytes()) + "\"},{\"id\":\"" + identifier + "#key-2\",\"relationshipType\":\"capabilityDelegation\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(key5.getPublicKey().toBytes()) + "\"},{\"id\":\"" + identifier + "#key-3\",\"relationshipType\":\"authentication\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(key3.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + identifier + "#did-root-key\"],\"authentication\":[\"" + identifier + "#did-root-key\",\"" + identifier + "#key-3\"],\"capabilityDelegation\":[\"" + identifier + "#key-2\"],\"service\":[{\"id\":\"" + identifier + "#service-1\",\"type\":\"LinkedDomains\",\"serviceEndpoint\":\"https://new.test.identity.com\"},{\"id\":\"" + identifier + "#service-2\",\"type\":\"LinkedDomains\",\"serviceEndpoint\":\"https://test2.identity.com\"}]}");
        assertEquals(expectedResult, doc.toJsonTree());

        assertNotNull(doc.getCreated());
        assertNotNull(doc.getUpdated());
        assertFalse(doc.getDeactivated());
        assertNotNull(doc.getVersionId());

    }


    @Test
    @DisplayName("successfully handles revoke service, verificationMethod and verificationRelationship events")
    void itTestRevokeServiceVerificationMethodAndRelationshipEvents() throws DidError, JsonProcessingException {
        PrivateKey key1 = PrivateKey.generateED25519();
        PrivateKey key2 = PrivateKey.generateED25519();
        PrivateKey key3 = PrivateKey.generateED25519();

        HcsDidMessage[] HcsDidMessage = {
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateDidOwnerEvent(identifier + "#did-root-key", identifier, privateKey.getPublicKey())
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateServiceEvent(
                                identifier + "#service-1",
                                ServiceType.LINKED_DOMAINS,
                                "https://test.identity.com"
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateServiceEvent(
                                identifier + "#service-2",
                                ServiceType.LINKED_DOMAINS,
                                "https://test2.identity.com"
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateVerificationMethodEvent(
                                identifier + "#key-1",
                                VerificationMethodSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                                identifier,
                                key1.getPublicKey()
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateVerificationRelationshipEvent(
                                identifier + "#key-2",
                                VerificationRelationshipType.CAPABILITY_DELEGATION,
                                VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                                identifier,
                                key2.getPublicKey()
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.CREATE,
                        identifier,
                        new HcsDidCreateVerificationRelationshipEvent(
                                identifier + "#key-3",
                                VerificationRelationshipType.AUTHENTICATION,
                                VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018,
                                identifier,
                                key3.getPublicKey()
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.REVOKE,
                        identifier,
                        new HcsDidRevokeServiceEvent(
                                identifier + "#service-1"
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.REVOKE,
                        identifier,
                        new HcsDidRevokeVerificationMethodEvent(
                                identifier + "#key-1"
                        )
                ),
                new HcsDidMessage(
                        DidMethodOperation.REVOKE,
                        identifier,
                        new HcsDidRevokeVerificationRelationshipEvent(
                                identifier + "#key-2",
                                VerificationRelationshipType.CAPABILITY_DELEGATION
                        )
                )
        };

        DidDocument doc = new DidDocument(identifier, HcsDidMessage);


        JsonNode expectedResult = new ObjectMapper().readTree("{\"@context\":\"https://www.w3.org/ns/did/v1\",\"id\":\"" + identifier + "\",\"verificationMethod\":[{\"id\":\"" + identifier + "#did-root-key\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa\"},{\"id\":\"" + identifier + "#key-3\",\"relationshipType\":\"authentication\",\"type\":\"Ed25519VerificationKey2018\",\"controller\":\"" + identifier + "\",\"publicKeyMultibase\":\"" + Hashing.Multibase.encode(key3.getPublicKey().toBytes()) + "\"}],\"assertionMethod\":[\"" + identifier + "#did-root-key\"],\"authentication\":[\"" + identifier + "#did-root-key\",\"" + identifier + "#key-3\"],\"service\":[{\"id\":\"" + identifier + "#service-2\",\"type\":\"LinkedDomains\",\"serviceEndpoint\":\"https://test2.identity.com\"}]}");
        assertEquals(expectedResult, doc.toJsonTree());

        assertNotNull(doc.getCreated());
        assertNotNull(doc.getUpdated());
        assertFalse(doc.getDeactivated());
        assertNotNull(doc.getVersionId());

    }
}
