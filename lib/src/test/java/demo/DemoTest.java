package demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.identity.hcs.MessageEnvelope;
import com.hedera.hashgraph.identity.hcs.did.HcsDid;
import com.hedera.hashgraph.identity.hcs.did.HcsDidEventMessageResolver;
import com.hedera.hashgraph.identity.hcs.did.HcsDidIntegrationTest;
import com.hedera.hashgraph.identity.hcs.did.HcsDidMessage;
import com.hedera.hashgraph.identity.hcs.did.event.service.ServiceType;
import com.hedera.hashgraph.identity.hcs.did.event.verificationMethod.VerificationMethodSupportedKeyType;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.VerificationRelationshipSupportedKeyType;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.VerificationRelationshipType;
import com.hedera.hashgraph.sdk.*;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@Tag("demo")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoTest {


    private final Client client = Client.forTestnet();
    private String didIdentifier;
    private PrivateKey didPrivateKey;


    public DemoTest() {

        String CONFIG_FILE = "demo.config.properties";
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

            // register did identifier & PK
            this.didIdentifier = prop.getProperty("DID_IDENTIFIER");
            if (!Strings.isNullOrEmpty(prop.getProperty("DID_PRIVATE_KEY")))
                this.didPrivateKey = PrivateKey.fromString(prop.getProperty("DID_PRIVATE_KEY"));


        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }


    @Test
    @DisplayName("generate primary key and register DID")
    void register() throws DidError, ReceiptStatusException, JsonProcessingException, PrecheckStatusException, TimeoutException {

        PrivateKey didPk = PrivateKey.generateED25519();

        HcsDid registeredDid = new HcsDid(null, didPk, client);
        registeredDid.register();

        System.out.printf("DID PRIVATE KEY: %s%n", didPk);
        System.out.printf("DID PUBLIC KEY: %s%n", didPk.getPublicKey().toString());
        System.out.printf("Registered DID Identifier: %s%n", registeredDid.getIdentifier());

    }

    @Test
    @DisplayName("resolve DID")
    void resolve() throws DidError, JsonProcessingException {

        HcsDid registeredDid = new HcsDid(this.didIdentifier, null, client);
        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));

        System.out.println();
        System.out.println("===================================================");
        System.out.println("DragonGlass Explorer:");
        System.out.printf("https://testnet.dragonglass.me/hedera/topics/%s%n", registeredDid.getTopicId().toString());

    }

    @Test
    @DisplayName("read DID event messages")
    void readMessages() throws DidError {

        HcsDid registeredDid = new HcsDid(this.didIdentifier, null, null);

        AtomicReference<List<MessageEnvelope<HcsDidMessage>>> messageRef = new AtomicReference<>(null);

        new HcsDidEventMessageResolver(registeredDid.getTopicId())
                .setTimeout(HcsDid.READ_TOPIC_MESSAGES_TIMEOUT)
                .whenFinished(messageRef::set)
                .execute(client);

        // Wait until mirror node resolves the DID.
        Awaitility.await().atMost(Duration.ofSeconds(30)).until(() -> messageRef.get() != null);

        messageRef.get().forEach((envelope) -> {
            var msg = envelope.open();

            System.out.println("\n===================================================\n");
            System.out.println("Message:");
            try {
                System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(msg.toJsonTree()));
                System.out.println();
                System.out.println("Event:");
                System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(msg.getEvent().toJsonTree()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        System.out.println();
        System.out.println("===================================================");
        System.out.println("DragonGlass Explorer:");
        System.out.printf("https://testnet.dragonglass.me/hedera/topics/%s%n", registeredDid.getTopicId().toString());


    }


    @Test
    @DisplayName("add service")
    void addService() throws DidError, JsonProcessingException {

        /*
          Build DID instance
         */
        HcsDid registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        String serviceIdentifier = "did:hedera:testnet:z6Mkik3aScXDEzSwQ5JuKVENDVm8q8o6yMKLS4KwGBAFhezq_0.0.34113681";

        /*
          Add Service
         */

        registeredDid.addService(serviceIdentifier + "#service-1", ServiceType.LINKED_DOMAINS, "https://example.com/vcs");

        System.out.println("\n");
        System.out.println("Added");
        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));


    }

    @Test
    @DisplayName("update service")
    void updateService() throws DidError, JsonProcessingException {

        /*
          Build DID instance
         */
        HcsDid registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        String serviceIdentifier = "did:hedera:testnet:z6Mkik3aScXDEzSwQ5JuKVENDVm8q8o6yMKLS4KwGBAFhezq_0.0.34113681";

        /*
          Update Service
         */

        registeredDid.updateService(serviceIdentifier + "#service-1", ServiceType.LINKED_DOMAINS, "https://test.com/did");

        System.out.println("\n");
        System.out.println("Updated");
        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));


    }

    @Test
    @DisplayName("revoke service")
    void revokeService() throws DidError, JsonProcessingException {

        /*
          Build DID instance
         */
        HcsDid registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        String serviceIdentifier = "did:hedera:testnet:z6Mkik3aScXDEzSwQ5JuKVENDVm8q8o6yMKLS4KwGBAFhezq_0.0.34113681";

        /*
          Revoke Service
         */

        registeredDid.revokeService(serviceIdentifier + "#service-1");

        System.out.println("\n");
        System.out.println("Revoked");
        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));

    }


    @Test
    @DisplayName("add Verification Method")
    void addVerificationMethod() throws DidError, JsonProcessingException {

        /*
          Build DID instance
         */
        HcsDid registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        var verificationMethodIdentifier = "did:hedera:testnet:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk_0.0.29617801";
        var verificationMethodPublicKey = HcsDid.stringToPublicKey("z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk");
        var updatedVerificationMethodPublicKey = HcsDid.stringToPublicKey("z6MkhHbhBBLdKGiGnHPvrrH9GL7rgw6egpZiLgvQ9n7pHt1P");

        /*
          Add Verification Method
         */

        registeredDid.addVerificationMethod(verificationMethodIdentifier + "#key-1", VerificationMethodSupportedKeyType.ED25519_VERIFICATION_KEY_2018, registeredDid.getIdentifier(), verificationMethodPublicKey);

        System.out.println("\n");
        System.out.println("Added");
        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));


    }

    @Test
    @DisplayName("update Verification Method")
    void updateVerificationMethod() throws DidError, JsonProcessingException {

        /*
          Build DID instance
         */
        HcsDid registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        var verificationMethodIdentifier = "did:hedera:testnet:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk_0.0.29617801";
        var updatedVerificationMethodPublicKey = HcsDid.stringToPublicKey("z6MkhHbhBBLdKGiGnHPvrrH9GL7rgw6egpZiLgvQ9n7pHt1P");


        /*
          Update Verification Method
         */

        registeredDid.updateVerificationMethod(verificationMethodIdentifier + "#key-1", VerificationMethodSupportedKeyType.ED25519_VERIFICATION_KEY_2018, registeredDid.getIdentifier(), updatedVerificationMethodPublicKey);

        System.out.println("\n");
        System.out.println("Updated");
        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));


    }

    @Test
    @DisplayName("add update revoke Verification Method")
    void revokeVerificationMethod() throws DidError, JsonProcessingException {

        /*
          Build DID instance
         */
        HcsDid registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        var verificationMethodIdentifier = "did:hedera:testnet:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk_0.0.29617801";

        /*
          Revoke Verification Method
         */

        registeredDid.revokeVerificationMethod(verificationMethodIdentifier + "#key-1");

        System.out.println("\n");
        System.out.println("Revoked");
        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));

    }

    @Test
    @DisplayName("add Verification Relationship")
    void addVerificationRelationship() throws DidError, JsonProcessingException {

        /*
          Build DID instance
         */
        HcsDid registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        var verificationRelationshipIdentifier = "did:hedera:testnet:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk_0.0.29617801";
        var verificationRelationshipPublicKey = HcsDid.stringToPublicKey("z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk");

        /*
          Add Verification Relationship
         */

        registeredDid.addVerificationRelationship(verificationRelationshipIdentifier + "#key-1", VerificationRelationshipType.AUTHENTICATION, VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018, registeredDid.getIdentifier(), verificationRelationshipPublicKey);

        System.out.println("\n");
        System.out.println("Added");
        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));


    }

    @Test
    @DisplayName("update Verification Relationship")
    void updateVerificationRelationship() throws DidError, JsonProcessingException {

        /*
          Build DID instance
         */
        HcsDid registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        var verificationRelationshipIdentifier = "did:hedera:testnet:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk_0.0.29617801";
        var updatedVerificationRelationshipPublicKey = HcsDid.stringToPublicKey("z6MkhHbhBBLdKGiGnHPvrrH9GL7rgw6egpZiLgvQ9n7pHt1P");

        /*
          Update Verification Relationship
         */

        registeredDid.updateVerificationRelationship(verificationRelationshipIdentifier + "#key-1", VerificationRelationshipType.AUTHENTICATION, VerificationRelationshipSupportedKeyType.ED25519_VERIFICATION_KEY_2018, registeredDid.getIdentifier(), updatedVerificationRelationshipPublicKey);

        System.out.println("\n");
        System.out.println("Updated");
        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));


    }

    @Test
    @DisplayName("revoke Verification Relationship")
    void revokeVerificationRelationship() throws DidError, JsonProcessingException {

        /*
          Build DID instance
         */
        HcsDid registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        var verificationRelationshipIdentifier = "did:hedera:testnet:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk_0.0.29617801";

        /*
          Revoke Verification Relationship
         */

        registeredDid.revokeVerificationRelationship(verificationRelationshipIdentifier + "#key-1", VerificationRelationshipType.AUTHENTICATION);

        System.out.println("\n");
        System.out.println("Revoked");
        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));

    }

    @Test
    @DisplayName("change DID ownership")
    void changeOwnership() throws DidError, JsonProcessingException, ReceiptStatusException, PrecheckStatusException, TimeoutException {

        /*

          Change DID Ownership, works under the following assumption.

          Current DID Owner transfers registered DID PrivateKey to New Owner using secure channel.
          New Owner performs change did owner operation with existing registered DID PrivateKey and new owners PrivateKey.

         */

        /*

          Change DID Ownership performs following tasks

          It transfers the ownership of DIDDocument and HCS Topic
          It updates Topic AdminKey and SubmitKey by signing updateTopicTransaction with both existing owner PrivateKey and new owner PrivateKey
          It also submits Update DIDOwner Event to HCS topic with new owner PublicKey. - of course singed by new owner PrivateKey
          Eventually, when DID Document get resolved, Update DIDOwner Event translates to DID Document controller/#did-root-key
         */

        /*
          Build DID instance
         */
        var existingOwnerDIDPrivateKey = this.didPrivateKey;
        HcsDid registeredDid = new HcsDid(this.didIdentifier, existingOwnerDIDPrivateKey, client);

        /*
          New Owner PrivateKey
         */
        var newOwnerDidPrivateKey = PrivateKey.generateED25519();
        var newOwnerIdentifier = "did:hedera:testnet:z6MkgUv5CvjRP6AsvEYqSRN7djB6p4zK9bcMQ93g5yK6Td7N_0.0.29613327";


        /*
          Change ownership
         */
        registeredDid.changeOwner(newOwnerIdentifier, newOwnerDidPrivateKey);

        /*
          Updated Did Doc
         */
        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));

        /*
          New Owner Information
         */
        System.out.println("New Owner Information");
        System.out.printf("DID PRIVATE KEY: %s%n", newOwnerDidPrivateKey);
        System.out.printf("DID PUBLIC KEY: %s%n", newOwnerDidPrivateKey.getPublicKey().toString());


        System.out.println();
        System.out.println("===================================================");
        System.out.println("DragonGlass Explorer:");
        System.out.printf("https://testnet.dragonglass.me/hedera/topics/%s%n", registeredDid.getTopicId().toString());

    }

    @Test
    @DisplayName("Delete DID")
    void delete() throws DidError, JsonProcessingException {

        /*
          Build DID instance
         */
        var registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        /*
          Delete DID
         */
        registeredDid.delete();

        System.out.printf("%s%n", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));


    }

}
