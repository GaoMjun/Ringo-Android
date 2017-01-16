package io.github.gaomjun.cvcamera;

import android.graphics.Matrix;
import android.hardware.Camera;
import android.provider.Settings;
import android.util.Log;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import io.github.gaomjun.cameraengine.CameraEngine;

/**
 * Created by qq on 21/11/2016.
 */

public class CVCamera {
    public CameraEngine cameraEngine = CameraEngine.getInstance();
    public FrameCallback delegate;

    private Camera.PreviewCallback cameraPreviewCallback  = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        Mat mat = new Mat(cameraEngine.previewHeight, cameraEngine.previewWidth, CvType.CV_8UC1);
                        mat.put(0, 0, data);
                        if (!mat.empty()) {
                            final Mat smallMat = mat.clone();
                            Imgproc.resize(smallMat, smallMat, new org.opencv.core.Size(128, 72));
                            delegate.processingFrame(smallMat);
                        }
                    }
                }
            }).start();
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
        if (!OpenCVLoader.initDebug()) {
            Log.d("TAG", "Internal OpenCV library not found");
        } else {
            Log.d("TAG", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public interface FrameCallback {
        void processingFrame(Mat mat);
    }
}
