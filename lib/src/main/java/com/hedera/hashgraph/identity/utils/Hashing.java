package com.hedera.hashgraph.identity.utils;


import java.nio.charset.StandardCharsets;


public class Hashing {
    public static class Multibase {
        public static String encode(byte[] data) {
            return io.ipfs.multibase.Multibase.encode(io.ipfs.multibase.Multibase.Base.Base58BTC, data);
        }

        public static byte[] decode(String encoded) {
            return io.ipfs.multibase.Multibase.decode(encoded);
        }
    }

    public static class Base64 {
        public static String encode(String encodedString) {
            return java.util.Base64.getEncoder().encodeToString(encodedString.getBytes(StandardCharsets.UTF_8));
        }

        public static byte[] decode(String decodedString) {
            return java.util.Base64.getDecoder().decode(decodedString);
        }

    }
}
