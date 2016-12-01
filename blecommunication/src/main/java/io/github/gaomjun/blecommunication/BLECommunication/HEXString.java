package io.github.gaomjun.blecommunication.BLECommunication;

import java.util.Locale;

/**
 * Created by qq on 30/11/2016.
 */

public class HEXString {
    public static byte[] hexString2Bytes(String s) {
        byte[] bytes;
        bytes = new byte[s.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }

        return bytes;
    }

    public static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase(Locale.getDefault());
        }
        return ret;
    }
}
