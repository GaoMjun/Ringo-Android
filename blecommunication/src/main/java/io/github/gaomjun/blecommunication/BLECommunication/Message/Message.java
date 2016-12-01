package io.github.gaomjun.blecommunication.BLECommunication.Message;

/**
 * Created by qq on 30/11/2016.
 */

public class Message {

    protected final static int MESSAGE_LENGTH = 20;

    protected static byte[] calculateCrc16(byte[] tx, int len) {
        char crc = 0x0;
        char[] crc_ta = { 0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7, 0x8108, 0x9129, 0xa14a,
                0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef };

        byte da;
        int i = 0;
        while (len-- != 0) {
            da = (byte) (((crc & 0xffff) >> 12));
            crc <<= 4;
            crc = (char) (crc ^ (crc_ta[da ^ (((tx[i] & 0xff) >> 4) & 0x0f)]));
            da = (byte) ((crc & 0xffff) >> 12);
            crc <<= 4;
            crc = (char) (crc ^ (crc_ta[da ^ (tx[i] & 0x0F)]));
            i++;
        }

        byte[] crc16Bytes = new byte[2];
        crc16Bytes[0] = (byte) ((crc & 0xff00) >>> 8);
        crc16Bytes[1] = (byte) (crc & 0x00ff);

        return crc16Bytes;
    }
}
