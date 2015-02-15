package io.github.jwifisd.eyefi;

import org.apache.commons.codec.binary.Hex;

public class Main2 {

    public static void main(String[] args) throws Exception {
        String cnonce = "58e91b1c81296b19324e2a3b0f40d279";
        String macaddress = "00185669c7ca";
        // macaddress = "0024d62d40c2";
        byte[] cnonceBytes = Hex.decodeHex(cnonce.toCharArray());
        byte[] uploadKey = Hex.decodeHex("00000000000000000000000000000000".toCharArray());

        System.out.println(Hex.encodeHexString(Main.md5(Hex.decodeHex((macaddress +cnonce+"00000000000000000000000000000000").toCharArray()))));

        if (1 == 0) {

            String expected = "edec35f9d8f5b00fd9a3ff763a47928b";
            for (long value1 = Long.MIN_VALUE; value1 < Long.MAX_VALUE; value1++) {
                for (long value2 = Long.MIN_VALUE; value2 < Long.MAX_VALUE; value2++) {
                    uploadKey[0] = (byte) ((value1 >> 0) & 0xFF);
                    uploadKey[1] = (byte) ((value1 >> 8) & 0xFF);
                    uploadKey[2] = (byte) ((value1 >> 16) & 0xFF);
                    uploadKey[3] = (byte) ((value1 >> 24) & 0xFF);
                    uploadKey[4] = (byte) ((value1 >> 32) & 0xFF);
                    uploadKey[5] = (byte) ((value1 >> 40) & 0xFF);
                    uploadKey[6] = (byte) ((value1 >> 48) & 0xFF);
                    uploadKey[7] = (byte) ((value1 >> 56) & 0xFF);
                    uploadKey[8] = (byte) ((value2 >> 0) & 0xFF);
                    uploadKey[9] = (byte) ((value2 >> 8) & 0xFF);
                    uploadKey[10] = (byte) ((value2 >> 16) & 0xFF);
                    uploadKey[11] = (byte) ((value2 >> 24) & 0xFF);
                    uploadKey[12] = (byte) ((value2 >> 32) & 0xFF);
                    uploadKey[13] = (byte) ((value2 >> 40) & 0xFF);
                    uploadKey[14] = (byte) ((value2 >> 48) & 0xFF);
                    uploadKey[15] = (byte) ((value2 >> 56) & 0xFF);
                    byte[] credential = Main.md5(Hex.decodeHex(macaddress.toCharArray()), cnonceBytes, uploadKey);
                    String credentialStr = Hex.encodeHexString(credential);
                    if (credentialStr.equals(expected)) {
                        System.out.println("found upload key " + Hex.encodeHexString(uploadKey));
                    }
                }
                System.out.println("loop");

            }
            byte[] credential = Main.md5(Hex.decodeHex(macaddress.toCharArray()), cnonceBytes, uploadKey);
            String credentialStr = Hex.encodeHexString(credential);
            System.out.println(credentialStr);
        }
    }
}
