package io.github.gaomjun.blecommunication.BLECommunication.Message;

import io.github.gaomjun.utils.TypeConversion.HEXString;

/**
 * Created by qq on 30/11/2016.
 */

public class SendMessage extends Message {
    private byte[] message = new byte[20];
    private byte[] header = new byte[]{(byte)0xab, (byte)0xcd};
    private byte[] commandBack = new byte[1];
    private byte[] trackingFlag = new byte[1];
    private byte[] trackingQuailty = new byte[1];
    private byte[] xoffset = new byte[4];
    private byte[] yoffset = new byte[4];
    private byte[] offset = new byte[5];
    private byte[] crc = new byte[2];

    public void setCommandBack(byte[] commandBack) {
        if (commandBack != null) {
            this.commandBack = commandBack;
        } else {
            this.commandBack = new byte[]{(byte) 0x00};
        }
    }

    public void setTrackingFlag(byte[] trackingFlag) {
        if (trackingFlag != null) {
            this.trackingFlag = trackingFlag;
        } else {
            this.trackingFlag = new byte[]{(byte) 0x00};
        }
    }

    public void setTrackingQuailty(byte[] trackingQuailty) {
        if (trackingQuailty != null) {
            this.trackingQuailty = trackingQuailty;
        } else {
            this.trackingQuailty = new byte[]{(byte) 0x00};
        }
    }

    public void setXoffset(byte[] xoffset) {
        if (xoffset != null) {
            this.xoffset = xoffset;
        } else {
            this.xoffset = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};;
        }
    }

    public void setYoffset(byte[] yoffset) {
        if (yoffset != null) {
            this.yoffset = yoffset;
        } else {
            this.yoffset = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};;
        }
    }

    private byte[] crc(byte[] message) {
        this.crc = calculateCrc16(message, MESSAGE_LENGTH-2);
        return crc;
    }

    private SendMessage() {

    }

    private volatile static SendMessage instance = null;

    public static SendMessage getInstance() {
        if (instance == null) {
            synchronized (SendMessage.class) {
                if (instance == null) {
                    instance = new SendMessage();
                }
            }
        }

        return instance;
    }

    public byte[] getMessage() {
        byte[] buff = new byte[20];

        int index = 0;
        System.arraycopy(header, 0, buff, index, header.length); index += header.length;
        System.arraycopy(commandBack, 0, buff, index, commandBack.length); index += commandBack.length;
        System.arraycopy(trackingFlag, 0, buff, index, trackingFlag.length); index += trackingFlag.length;
        System.arraycopy(trackingQuailty, 0, buff, index, trackingQuailty.length); index += trackingQuailty.length;
        System.arraycopy(xoffset, 0, buff, index, xoffset.length); index += xoffset.length;
        System.arraycopy(yoffset, 0, buff, index, yoffset.length); index += yoffset.length;
        System.arraycopy(offset, 0, buff, index, offset.length); index += offset.length;
        System.arraycopy(crc(buff), 0, buff, index, crc.length);

        message = buff;

        return message;
    }

    public String getMessageHexString() {
        return HEXString.bytes2HexString(message);
    }
}
