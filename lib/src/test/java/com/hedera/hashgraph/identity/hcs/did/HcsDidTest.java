package com.hedera.hashgraph.identity.hcs.did;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    @DisplayName("throws error if DID is already registered")
    void itTestsIfDIDIsAlreadyRegistered() throws ReceiptStatusException, JsonProcessingException, PrecheckStatusException, TimeoutException, DidError {
        PrivateKey privateKey = this.operatorKey;
        HcsDid did = new HcsDid(null, privateKey, this.client);

        did.register();
        DidError error = assertThrows(DidError.class, did::register);
        assertEquals("", error.getMessage());
    }
}
