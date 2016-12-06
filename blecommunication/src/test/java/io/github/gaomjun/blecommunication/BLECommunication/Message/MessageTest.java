package io.github.gaomjun.blecommunication.BLECommunication.Message;

import org.junit.Before;
import org.junit.Test;

import io.github.gaomjun.utils.TypeConversion.HEXString;

import static org.junit.Assert.*;

/**
 * Created by qq on 1/12/2016.
 */
public class MessageTest {
    private Message message;
    private final String messageString = "abcd000100000000000000000000000000001157";

    @Before
    public void setUp() throws Exception {
        message = new Message();
    }

    @Test
    public void calculateCrc16() throws Exception {
        byte[] bytes = HEXString.hexString2Bytes(messageString);

        byte[] crc16Bytes = Message.calculateCrc16(bytes, bytes.length - 2);

        String hexString = HEXString.bytes2HexString(crc16Bytes);

        String substring = messageString.substring(18*2);
        assertEquals(substring, hexString);
    }

}