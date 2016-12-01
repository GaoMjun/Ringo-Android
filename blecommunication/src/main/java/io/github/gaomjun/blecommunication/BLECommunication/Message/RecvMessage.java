package io.github.gaomjun.blecommunication.BLECommunication.Message;

import android.util.Log;

import io.github.gaomjun.blecommunication.BLECommunication.HEXString;

/**
 * Created by qq on 30/11/2016.
 */

public class RecvMessage extends Message {
    private byte[] message = new byte[20];
    private byte[] header = new byte[]{(byte)0x9a, (byte)0xbc};
    private byte[] command = new byte[1];
    private byte[] gimbalStatus = new byte[1];
    private byte[] gimbalMode = new byte[1];
    private byte[] deviceMode = new byte[1];
    private byte[] firmwareVersion = new byte[1];
    private byte[] gimbalRollAngle = new byte[2];
    private byte[] gimbalPitchAngle = new byte[2];
    private byte[] gimbalHeaderAngle = new byte[2];
    private byte[] offset = new byte[5];
    private byte[] crc = new byte[2];

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public boolean checkMessage() {
        if (message[0] != header[0] || message[1] != header[1]) {
            Log.d("checkMessage", "header invalid");
            return false;
        }

        byte[] crc = calculateCrc16(message, MESSAGE_LENGTH-2);
        if (message[18] != crc[0] || message[19] != crc[1]) {
            Log.d("checkMessage", "crc invalid");
            return false;
        }

        return true;
    }

    private RecvMessage() {

    }

    private volatile static RecvMessage instance = null;

    public static RecvMessage getInstance() {
        if (instance == null) {
            synchronized (SendMessage.class) {
                if (instance == null) {
                    instance = new RecvMessage();
                }
            }
        }

        return instance;
    }

    public String getMessageHexString() {
        return HEXString.bytes2HexString(message);
    }
}
