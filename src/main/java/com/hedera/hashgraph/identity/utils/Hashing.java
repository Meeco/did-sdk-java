package com.hedera.hashgraph.identity.utils;

import io.ipfs.multibase.Multibase;

public class Hashing {
    public static class MultibaseClass {
        public static String encode(byte[] data) {
            return Multibase.encode(Multibase.Base.Base58BTC, data);
        }

        byte[] decode(String encoded) {
            return Multibase.decode(encoded);
        }
    }
}
