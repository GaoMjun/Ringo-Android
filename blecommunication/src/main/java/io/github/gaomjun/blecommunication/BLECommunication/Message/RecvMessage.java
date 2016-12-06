package io.github.gaomjun.blecommunication.BLECommunication.Message;

import android.util.Log;

import java.util.Arrays;

import io.github.gaomjun.utils.TypeConversion.HEXString;
import io.github.gaomjun.utils.TypeConversion.ByteArray;

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

    private byte[] preCommand = new byte[1];

    public void setMessage(byte[] message) {
        this.message = message;

        setCommand(ByteArray.subByteArray(message, 2, 1));

        setGimbalStatus(ByteArray.subByteArray(message, 3, 1));

        setGimbalMode(ByteArray.subByteArray(message, 4, 1));

        setDeviceMode(ByteArray.subByteArray(message, 5, 1));

        setFirmwareVersion(ByteArray.subByteArray(message, 6, 1));

        setGimbalRollAngle(ByteArray.subByteArray(message, 7, 2));

        setGimbalPitchAngle(ByteArray.subByteArray(message, 9, 2));

        setGimbalHeaderAngle(ByteArray.subByteArray(message, 11, 2));
    }

    private void setCommand(byte[] command) {
        this.command = command;
    }

    public byte[] getCommand() {
        byte[] cmd = {(byte) 0x00};

        if (Arrays.equals(command, GimbalMobileBLEProtocol.REMOTECOMMAND_CAPTURE)) {
            if (Arrays.equals(preCommand, GimbalMobileBLEProtocol.REMOTECOMMAND_CLEAR)) {
                cmd = GimbalMobileBLEProtocol.REMOTECOMMAND_CAPTURE;
            }

        } else if (Arrays.equals(command, GimbalMobileBLEProtocol.REMOTECOMMAND_RECORD)) {
            if (Arrays.equals(preCommand, GimbalMobileBLEProtocol.REMOTECOMMAND_CLEAR)) {
                cmd = GimbalMobileBLEProtocol.REMOTECOMMAND_RECORD;
            }
        } else {
            cmd = GimbalMobileBLEProtocol.REMOTECOMMAND_CLEAR;
        }

        preCommand = command;

        return cmd;
    }

    private void setGimbalStatus(byte[] gimbalStatus) {
        this.gimbalStatus = gimbalStatus;
    }

    public byte[] getGimbalStatus() {
        byte[] gbStatus = {(byte) 0x00};

        if (Arrays.equals(gimbalStatus, GimbalMobileBLEProtocol.GIMBALSTATUS_STOP)) {
            gbStatus = GimbalMobileBLEProtocol.GIMBALSTATUS_STOP;
        } else if (Arrays.equals(gimbalStatus, GimbalMobileBLEProtocol.GIMBALSTATUS_PAUSE)) {
            gbStatus = GimbalMobileBLEProtocol.GIMBALSTATUS_PAUSE;
        } else if (Arrays.equals(gimbalStatus, GimbalMobileBLEProtocol.GIMBALSTATUS_RUN)) {
            gbStatus = GimbalMobileBLEProtocol.GIMBALSTATUS_RUN;
        } else if (Arrays.equals(gimbalStatus, GimbalMobileBLEProtocol.GIMBALSTATUS_NEVERRUN)) {
            gbStatus = GimbalMobileBLEProtocol.GIMBALSTATUS_NEVERRUN;
        }
        return gbStatus;
    }

    private void setDeviceMode(byte[] deviceMode) {
        this.deviceMode = deviceMode;
    }

    private void setFirmwareVersion(byte[] firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    private void setGimbalMode(byte[] gimbalMode) {
        this.gimbalMode = gimbalMode;
    }

    public byte[] getGimbalMode() {
        byte[] gbMode = {(byte) 0x00};

        if (Arrays.equals(gimbalMode, GimbalMobileBLEProtocol.GIMBALMODE_FPV)) {
            gbMode = GimbalMobileBLEProtocol.GIMBALMODE_FPV;
        } else if (Arrays.equals(gimbalMode, GimbalMobileBLEProtocol.GIMBALMODE_PANFOLLOW)) {
            gbMode = GimbalMobileBLEProtocol.GIMBALMODE_PANFOLLOW;
        } else if (Arrays.equals(gimbalMode, GimbalMobileBLEProtocol.GIMBALMODE_PANLOOK)) {
            gbMode = GimbalMobileBLEProtocol.GIMBALMODE_PANLOOK;
        } else if (Arrays.equals(gimbalMode, GimbalMobileBLEProtocol.GIMBALMODE_FACEFOLLOW)) {
            gbMode = GimbalMobileBLEProtocol.GIMBALMODE_FACEFOLLOW;
        }
        return gbMode;
    }

    private void setGimbalRollAngle(byte[] gimbalRollAngle) {
        this.gimbalRollAngle = gimbalRollAngle;
    }

    private void setGimbalPitchAngle(byte[] gimbalPitchAngle) {
        this.gimbalPitchAngle = gimbalPitchAngle;
    }

    private void setGimbalHeaderAngle(byte[] gimbalHeaderAngle) {
        this.gimbalHeaderAngle = gimbalHeaderAngle;
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
