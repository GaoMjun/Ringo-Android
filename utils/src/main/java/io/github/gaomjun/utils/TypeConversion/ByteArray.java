package io.github.gaomjun.utils.TypeConversion;

/**
 * Created by qq on 2/12/2016.
 */

public final class ByteArray {
    public final static byte[] subByteArray(byte[] byteArray, int position, int length) {
        byte[] subArray = new byte[length];

        System.arraycopy(byteArray, position, subArray, 0, length);

        return subArray;
    }
}
