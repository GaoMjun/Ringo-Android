package io.github.gaomjun.utils.TypeConversion;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by qq on 2/12/2016.
 */
public class TypeConversionTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void intToBytes() throws Exception {
        byte[] bytes = TypeConversion.intToBytes(220);
        String hexString = HEXString.bytes2HexString(bytes);

        byte[] shouldBytes = new byte[] {(byte) 0x00, (byte) 0x00, (byte)0x03, (byte)0xE8};
        String s = HEXString.bytes2HexString(shouldBytes);

        String intHexString = "fa000000";
        byte[] bytes1 = HEXString.hexString2Bytes(intHexString);

        assertArrayEquals(bytes, shouldBytes);
    }

}