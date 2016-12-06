package io.github.gaomjun.utils.TypeConversion;

/**
 * Created by qq on 2/12/2016.
 */

public final class TypeConversion {

    public final static byte[] intToBytes(int num) {
        byte[] ret = new byte[4];
        ret[0] = (byte) (num & 0xFF);
        ret[1] = (byte) ((num >> 8) & 0xFF);
        ret[2] = (byte) ((num >> 16) & 0xFF);
        ret[3] = (byte) ((num >> 24) & 0xFF);
        return ret;
    }
}
