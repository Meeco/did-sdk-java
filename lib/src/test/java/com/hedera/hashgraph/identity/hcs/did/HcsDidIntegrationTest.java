package com.hedera.hashgraph.identity.hcs.did;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

@Disabled
public class HcsDidIntegrationTest {

    final AccountId operatorId = AccountId.fromString("0.0.12...");
    final PrivateKey operatorKey = PrivateKey.fromString("302e02...");
    final List<String> mirrorNetworks = List.of("hcs.testnet.mirrornode.hedera.com:5600");

    final Client client = Client.forTestnet();

    @Test
    @DisplayName("register did")
    void registerDID() throws ReceiptStatusException, JsonProcessingException, PrecheckStatusException, TimeoutException, DidError {
        HcsDid did = new HcsDid(null, this.operatorKey, this.client);

        // register
        String identifier = did.register().getIdentifier();
        System.out.println(identifier);

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(did.resolve().toJsonTree()));

    }


    @Test
    @DisplayName("resolve did")
    void resolveDID() throws JsonProcessingException, DidError {
        String identifier = "did:hedera:testnet:z6Mkg6N8h78J9vBFPv8inrnwikWSph4un3SnofPzrPQzbTDX_0.0.34099687";
        HcsDid registeredDid = new HcsDid(identifier, null, this.client);

        //resolve did
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(registeredDid.resolve().toJsonTree()));

    }
}
