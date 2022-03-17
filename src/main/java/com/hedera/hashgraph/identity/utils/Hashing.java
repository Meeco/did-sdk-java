package com.hedera.hashgraph.identity.utils;

import io.ipfs.multibase.Multibase;

public class Hashing {
    static class MultibaseClass {
        String encode(byte[] data) {
            return Multibase.encode(Multibase.Base.Base58BTC, data);
        }

        byte[] decode(String encoded) {
            return Multibase.decode(encoded);
        }
    }
}
