package io.github.gaomjun.blecommunication.BLECommunication.Message;

/**
 * Created by qq on 2/12/2016.
 */

public final class GimbalMobileBLEProtocol {
    // gimbal to mobile
    public final static byte[] REMOTECOMMAND_CAPTURE = {(byte) 0x11};
    public final static byte[] REMOTECOMMAND_RECORD = {(byte) 0x12};
    public final static byte[] REMOTECOMMAND_CLEAR = {(byte) 0x00};

    public final static byte[] GIMBALSTATUS_STOP = {(byte) 0X00};
    public final static byte[] GIMBALSTATUS_PAUSE = {(byte) 0x01};
    public final static byte[] GIMBALSTATUS_RUN = {(byte) 0x02};
    public final static byte[] GIMBALSTATUS_NEVERRUN = {(byte) 0x03};

    public final static byte[] GIMBALMODE_FPV = {(byte) 0x00};
    public final static byte[] GIMBALMODE_PANFOLLOW = {(byte) 0x01};
    public final static byte[] GIMBALMODE_PANLOOK = {(byte) 0x02};
    public final static byte[] GIMBALMODE_FACEFOLLOW = {(byte) 0x03};

    // mobile to gimbal
    public final static byte[] COMMANDBACK_CAPTRUE_OK = REMOTECOMMAND_CAPTURE;
    public final static byte[] COMMANDBACK_CAPTRUE_ERR = {(byte) (REMOTECOMMAND_CAPTURE[0] | (byte) 0x80)} ;
    public final static byte[] COMMANDBACK_RECORD_OK = REMOTECOMMAND_RECORD;
    public final static byte[] COMMANDBACK_RECORD_ERR = {(byte) (REMOTECOMMAND_RECORD[0] | (byte) 0x80)};
    public final static byte[] COMMADNBACK_CLEAR = REMOTECOMMAND_CLEAR;

    public final static byte[] TRACKING_FLAG_ON = {(byte) 0x01};
    public final static byte[] TRACKING_FLAG_OFF = {(byte) 0x00};

    public final static byte[] TRACKING_QUALITY_GOOD = {(byte) 0x01};
    public final static byte[] TRACKING_QUALITY_WEAK = {(byte) 0x00};
}
