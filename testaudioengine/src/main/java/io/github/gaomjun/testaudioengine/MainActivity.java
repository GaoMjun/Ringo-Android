package io.github.gaomjun.testaudioengine;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

//    private AudioEngine audioEngine = new AudioEngine(new AudioConfiguration());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        audioEngine.setPcmDataListener(new PCMDataListener());
//        audioEngine.setAacDataListener(new AACDataListener());
//        audioEngine.setEncoding(true);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                audioEngine.start();
//            }
//        }, 2000);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                audioEngine.stop();
//            }
//        }, 10000);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                audioEngine.start();
//            }
//        }, 12000);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                audioEngine.stop();
//            }
//        }, 20000);
    }

//    private class PCMDataListener implements AudioEngine.PCMDataListener {
//
//        @Override
//        public void onPCMData(@NotNull byte[] data, int size, long timestamp) {
//            System.out.println("onPCMData " + size + " " + timestamp);
//        }
//    }
//
//    private class AACDataListener implements AudioEngine.AACDataListener {
//
//        @Override
//        public void onAACData(@NotNull ByteBuffer byteBuffer, @NotNull MediaCodec.BufferInfo info) {
//            byte[] aacData = new byte[info.size];
//            byteBuffer.get(aacData);
//
//            System.out.println("onAACData " + info.size + " " + info.presentationTimeUs);
//        }
//    }
}
