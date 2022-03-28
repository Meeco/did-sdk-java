package com.hedera.hashgraph.identity.utils;


import com.google.common.primitives.Bytes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class Hashing {


    public static class Multibase {

        // values retrived from https://raw.githubusercontent.com/multiformats/multicodec/master/table.csv
        //_name = "ed25519-pub";
        //_code = 0xed;
        private static final byte[] codeBytes = new byte[]{-19, 1};

        public static String encode(byte[] data) {
            return io.ipfs.multibase.Multibase.encode(io.ipfs.multibase.Multibase.Base.Base58BTC, Bytes.concat(codeBytes, data));
        }

        public static byte[] decode(String encoded) {
            byte[] result = io.ipfs.multibase.Multibase.decode(encoded);
            // remove bytes
            return Arrays.copyOfRange(result, codeBytes.length - 1, result.length);
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
