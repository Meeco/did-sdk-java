package com.hedera.hashgraph.identity.utils;

import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
public class HashingTest {

    @Test
    void MultibaseMultiCodec() {
        PrivateKey privateKey = PrivateKey.generateED25519();

        byte[] publickeybytes = privateKey.getPublicKey().toBytes();
        String base58btcEncodedString = Hashing.Multibase.encode(publickeybytes);
        byte[] decodedPublicKeyBytes = Hashing.Multibase.decode(base58btcEncodedString);


        // z is for base58btc & 6Mk is for ed25519 pub key
        assertTrue(base58btcEncodedString.startsWith("z6Mk"));
        assertArrayEquals(publickeybytes, decodedPublicKeyBytes);
    }

}
