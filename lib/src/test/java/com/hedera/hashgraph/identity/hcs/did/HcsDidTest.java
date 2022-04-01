package com.hedera.hashgraph.identity.hcs.did;

import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class HcsDidTest {
    final AccountId operatorId = AccountId.fromString("0.0.12710106");
    final PrivateKey operatorKey = PrivateKey.fromString("302e020100300506032b657004220420bc45334a1313725653d3513fcc67edb15f76985f537ca567e2177b0be9906d49");
    final List<String> mirrorNetworks = List.of("hcs.testnet.mirrornode.hedera.com:5600");

    final Client client = Client.forTestnet();

    HcsDidTest() throws InterruptedException {
        this.client.setMirrorNetwork(this.mirrorNetworks);
        this.client.setOperator(this.operatorId, this.operatorKey);
    }


    @Test
    @DisplayName("throws error because of missing identifier and privateKey")
    void testErrorWhenMissingIdentifierAndPk() throws DidError {
        Assertions.assertThrowsExactly(DidError.class, () -> new HcsDid(null, null, null), "identifier and privateKey cannot both be empty");
    }

    @Test
    @DisplayName("successfully builds HcsDid with private key only")
    void testBuildWithPk() throws DidError {
        PrivateKey privateKey = PrivateKey.generateED25519();
        HcsDid did = new HcsDid(null, privateKey, null);

        Assertions.assertNull(did.getIdentifier());
        Assertions.assertEquals(privateKey, did.getPrivateKey());
        Assertions.assertNull(did.getClient());
        Assertions.assertNull(did.getTopicId());
        Assertions.assertNull(did.getNetwork());

    }

}
