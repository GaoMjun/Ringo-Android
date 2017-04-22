package io.github.gaomjun.cvcamera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import io.github.gaomjun.audioengine.AudioEngine;
import io.github.gaomjun.cameraengine.CameraEngine;
import io.github.gaomjun.live.encodeConfiguration.VideoConfiguration;
import io.github.gaomjun.live.h264Encoder.H264Encoder;

/**
 * Created by qq on 21/11/2016.
 */

public class CVCamera {

    private H264Encoder h264Encoder = new H264Encoder(VideoConfiguration.instance());

    private AudioEngine audioEngine = new AudioEngine();
    private boolean audioEngineStarted = false;

    public CameraEngine cameraEngine = CameraEngine.getInstance();
    public boolean startLive = false;
    public boolean startTracking = false;
    public FrameCallback delegate;

    private ExecutorService trackingThreadPool = Executors.newCachedThreadPool();

    private Mat matBuffer;

    private Camera.PreviewCallback cameraPreviewCallback  = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
//            System.out.println("onPreviewFrame");

            if (startLive) {
                h264Encoder.encoding(data, cameraEngine.previewWidth, cameraEngine.previewHeight, System.currentTimeMillis());

                if (!audioEngineStarted) {
                    audioEngineStarted = true;
                    audioEngine.start();
                }
            }

            if (startTracking) {
                if (trackingThreadPool == null) trackingThreadPool = Executors.newCachedThreadPool();
                trackingThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (matBuffer == null) matBuffer = new Mat(cameraEngine.previewHeight, cameraEngine.previewWidth, CvType.CV_8UC1);
                        matBuffer.put(0, 0, data);
                        final Mat smallMat = new Mat();
                        Imgproc.resize(matBuffer, smallMat, new org.opencv.core.Size(128, 72), 0, 0, Imgproc.INTER_NEAREST);
                        if (cameraEngine.isFrontCamera()) Core.flip(smallMat, smallMat, 1);
                        Imgproc.equalizeHist(smallMat, smallMat);
                        delegate.processingFrame(smallMat);
                    }
                });
            }

            camera.addCallbackBuffer(data);
        }
    };

    private LoaderCallbackInterface mLoaderCallback = new LoaderCallbackInterface() {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.d("onManagerConnected", "OpenCV loaded successfully");
                    cameraEngine.setPreviewCallback(cameraPreviewCallback);

                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPackageInstall(int operation, InstallCallbackInterface callback) {

        }
    };

    public CVCamera() {
        if (OpenCVLoader.initDebug()) {
            Log.d("TAG", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d("TAG", "Internal OpenCV library not found");
        }
    }

    public interface FrameCallback {
        void processingFrame(Mat mat);
    }
}
