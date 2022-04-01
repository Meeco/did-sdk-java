package com.hedera.hashgraph.identity.hcs.did;

import com.hedera.hashgraph.identity.DidError;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HcsDidTest {

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
