package io.github.gaomjun.utils.TypeConversion;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by qq on 2/12/2016.
 */
public class ByteArrayTest {
    @Test
    public void subByteArray() throws Exception {
        byte[] byteArray = {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05};

        byte[] subByteArray = ByteArray.subByteArray(byteArray, 1, 2);

        byte[] shouldSubArray = {(byte) 0x02, (byte) 0x03};

        assertArrayEquals(subByteArray, shouldSubArray);
    }

}