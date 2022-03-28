package com.hedera.hashgraph.identity.hcs.did.event.service;

import com.google.common.primitives.Bytes;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.Test;

public class TestTest {

    @Test
    void itTargetsService() {
        byte[] result = io.ipfs.multibase.Multibase.decode("z6MkogVzoGJMVVLhaz82cA5jZQKAAqUghhCrpzkSDFDwxfJa");


        for (int i = 0; i < result.length; i++) {
            System.out.println("Element at " + i + ": " + result[i]);
        }
    }

    @Test
    void test() {
        PrivateKey privateKey = PrivateKey.fromString(
                "302e020100300506032b6570042204209044d8f201e4b0aa7ba8ed577b0334b8cb6e38aad6c596171b5b1246737f5079"
        );

        byte[] by = new byte[2];
        by[0] = -19;
        by[1] = 1;

        System.out.println(io.ipfs.multibase.Multibase.encode(io.ipfs.multibase.Multibase.Base.Base58BTC, Bytes.concat(by, privateKey.getPublicKey().toBytes())));
    }
}
