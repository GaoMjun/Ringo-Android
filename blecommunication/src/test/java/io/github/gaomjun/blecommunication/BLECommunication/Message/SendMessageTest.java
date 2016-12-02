package io.github.gaomjun.blecommunication.BLECommunication.Message;

import org.junit.Before;
import org.junit.Test;

import io.github.gaomjun.utils.TypeConversion.TypeConversion;

import static org.junit.Assert.*;

/**
 * Created by qq on 2/12/2016.
 */
public class SendMessageTest {
    private SendMessage sendMessage = SendMessage.getInstance();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void getMessage() throws Exception {
        sendMessage.setXoffset(TypeConversion.intToBytes(100));
        sendMessage.setYoffset(TypeConversion.intToBytes(-200));

        sendMessage.setTrackingFlag(new byte[]{(byte)0x01});

        byte[] bytes = sendMessage.getMessage();

        String message = sendMessage.getMessageHexString();

        assertEquals(message, "ABCD00010000000064FFFFFF38000000000068B8");

    }

}