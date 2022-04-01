package com.hedera.hashgraph.identity.hcs.did;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

@Disabled
public class HcsDidTest {
    final AccountId operatorId = AccountId.fromString("0.0.2xxx");
    final PrivateKey operatorKey = PrivateKey.fromString("302xxx");
    final List<String> mirrorNetworks = List.of("hcs.testnet.mirrornode.hedera.com:5600");

    final Client client = Client.forTestnet();

    HcsDidTest() throws InterruptedException {
        this.client.setMirrorNetwork(this.mirrorNetworks);
        this.client.setOperator(this.operatorId, this.operatorKey);
    }

    @Test
    @DisplayName("register did")
    void registerDID() throws ReceiptStatusException, JsonProcessingException, PrecheckStatusException, TimeoutException, DidError {
        HcsDid did = new HcsDid(null, this.operatorKey, this.client);

        // register
        String identifier = did.register().getIdentifier();
        System.out.println(identifier);

        System.out.println(did.resolve().toJSON());

    }

    //TODO: WIP

//    @Test
//    @DisplayName("resolve did")
//    void resolveDID() throws JsonProcessingException, DidError {
//        String identifier = "did:hedera:testnet:z6MkhHbhBBLdKGiGnHPvrrH9GL7rgw6egpZiLgvQ9n7pHt1P_0.0.34099347";
//        HcsDid did = new HcsDid(identifier, null, this.client);
//
//        //resolve did
//        System.out.println(did.resolve().toJSON());
//
//    }
}
