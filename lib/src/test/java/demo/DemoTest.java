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
    private final String CONFIG_FILE = "demo.config.properties";
    private String didIdentifier;
    private PrivateKey didPrivateKey;


    public DemoTest() {

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

        System.out.println(String.format("DID PRIVATE KEY: %s", didPk));
        System.out.println(String.format("DID PUBLIC KEY: %s", didPk.getPublicKey().toString()));
        System.out.println(String.format("Registered DID Identifier: %s", registeredDid.getIdentifier()));

    }

    @Test
    @DisplayName("resolve DID")
    void resolve() throws DidError, JsonProcessingException {

        HcsDid registeredDid = new HcsDid(this.didIdentifier, null, client);
        System.out.println(String.format("%s", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree())));

        System.out.println();
        System.out.println("===================================================");
        System.out.println("DragonGlass Explorer:");
        System.out.println(String.format("https://testnet.dragonglass.me/hedera/topics/%s", registeredDid.getTopicId().toString()));

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
                System.out.println(String.format("%s", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(msg.toJsonTree())));
                System.out.println();
                System.out.println("Event:");
                System.out.println(String.format("%s", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(msg.getEvent().toJsonTree())));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        System.out.println();
        System.out.println("===================================================");
        System.out.println("DragonGlass Explorer:");
        System.out.println(String.format("https://testnet.dragonglass.me/hedera/topics/%s", registeredDid.getTopicId().toString()));


    }


    @Test
    @DisplayName("add update revoke service")
    void service() throws DidError, JsonProcessingException {

        /**
         * Build DID instance
         */
        HcsDid registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        String serviceIdentifier = "did:hedera:testnet:z6Mkik3aScXDEzSwQ5JuKVENDVm8q8o6yMKLS4KwGBAFhezq_0.0.34113681";

        /**
         * Add Service
         */

        registeredDid.addService(serviceIdentifier + "#service-1", ServiceType.LINKED_DOMAINS, "https://example.com/vcs");

        System.out.println("\n");
        System.out.println("Added");
        System.out.println(String.format("%s", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree())));


        /**
         * Update Service
         */

        registeredDid.updateService(serviceIdentifier + "#service-1", ServiceType.LINKED_DOMAINS, "https://test.com/did");

        System.out.println("\n");
        System.out.println("Updated");
        System.out.println(String.format("%s", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree())));

        /**
         * Revoke Service
         */

        registeredDid.revokeService(serviceIdentifier + "#service-1");

        System.out.println("\n");
        System.out.println("Revoked");
        System.out.println(String.format("%s", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree())));

    }

    @Test
    @DisplayName("add update revoke Verification Method")
    void verificationMethod() throws DidError, JsonProcessingException {

        /**
         * Build DID instance
         */
        HcsDid registeredDid = new HcsDid(this.didIdentifier, this.didPrivateKey, client);

        var verificationMethodIdentifier = "did:hedera:testnet:z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk_0.0.29617801";
        var verificationMethodPublicKey = HcsDid.stringToPublicKey("z6Mkkcn1EDXc5vzpmvnQeCKpEswyrnQG7qq59k92gFRm1EGk");
        var updatedVerificationMethodPublicKey = HcsDid.stringToPublicKey("z6MkhHbhBBLdKGiGnHPvrrH9GL7rgw6egpZiLgvQ9n7pHt1P");

        /**
         * Add Service
         */

        registeredDid.addVerificationMethod(verificationMethodIdentifier + "#key-1", VerificationMethodSupportedKeyType.ED25519_VERIFICATION_KEY_2018, registeredDid.getIdentifier(), verificationMethodPublicKey);

        System.out.println("\n");
        System.out.println("Added");
        System.out.println(String.format("%s", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree())));


        /**
         * Update Service
         */

        registeredDid.updateVerificationMethod(verificationMethodIdentifier + "#key-1", VerificationMethodSupportedKeyType.ED25519_VERIFICATION_KEY_2018, registeredDid.getIdentifier(), updatedVerificationMethodPublicKey);

        System.out.println("\n");
        System.out.println("Updated");
        System.out.println(String.format("%s", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree())));

        /**
         * Revoke Service
         */

        registeredDid.revokeVerificationMethod(verificationMethodIdentifier + "#key-1");

        System.out.println("\n");
        System.out.println("Revoked");
        System.out.println(String.format("%s", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree())));

    }

}
