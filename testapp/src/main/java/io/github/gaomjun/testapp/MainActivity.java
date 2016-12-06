package io.github.gaomjun.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.nio.ByteBuffer;

import io.github.gaomjun.blecommunication.BLECommunication.BLEDriven;
import io.github.gaomjun.utils.TypeConversion.HEXString;
import io.github.gaomjun.blecommunication.BLECommunication.Message.RecvMessage;
import io.github.gaomjun.blecommunication.BLECommunication.Message.SendMessage;

public class MainActivity extends Activity {

    private BLEDriven bleDriven = null;
    private SendMessage sendMessage = SendMessage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bleDriven = new BLEDriven(MainActivity.this);
        bleDriven.setRecvCallback(new RecvDataListener());
        bleDriven.setConnectingStatusCallback(new BLEConnectingListener());
        bleDriven.scanDevices();

        byte[] bytes = ByteBuffer.allocate(4).putInt(-1001).array();
        String hexString = HEXString.bytes2HexString(bytes);
        Log.d("HEXString", hexString);
    }

    private class RecvDataListener implements BLEDriven.RecvCallback {

        @Override
        public void onRecvData(RecvMessage recvMessage) {
            Log.d("recv", recvMessage.getMessageHexString());


        }
    }

    private class BLEConnectingListener implements BLEDriven.ConnectingStatusCallback {

        @Override
        public void onConnecting(int status) {
            switch (status) {
                case BLEDriven.CONNECTED:
                    Log.d("onConnecting", "CONNECTED");
                    (new SendThread()).start();
                    break;
                case BLEDriven.CONNECTING:
                    Log.d("onConnecting", "CONNECTING");
                    break;
                case BLEDriven.DISCONNECTED:
                    Log.d("onConnecting", "DISCONNECTED");
                    break;
            }
        }
    }

    private class SendThread extends Thread {

        @Override
        public void run() {
            super.run();

            while (!isInterrupted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bleDriven.send(sendMessage.getMessage());
                Log.d("send", sendMessage.getMessageHexString());
            }
        }
    }
}
