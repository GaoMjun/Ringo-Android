package io.github.gaomjun.cameraengine;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qq on 18/11/2016.
 */

public class CameraEngine {
//    private byte[] cameraCallbackBuffer1;
//    private byte[] cameraCallbackBuffer2;
    private static final int RECORD_FINISH = 0;
    public Context context;
    private volatile static CameraEngine instance = null;
    public static int CAMERA_BACK = 0;
    public static int CAMERA_FRONT = 1;
    private int cameraId = CAMERA_FRONT;
    private static SurfaceTexture surfaceTexture;
    public static int previewWidth;
    public static int previewHeight;
    private static Camera.PreviewCallback previewCallback;
    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
        }
    };
    private MediaRecorder mediaRecorder;
    private MediaRecorder.OnInfoListener mediaRecorderInfoListener =
            new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Log.d("onInfo", "what" + what + " " + "extra" + extra);
        }
    };
    private MediaRecorder.OnErrorListener mediaRecorderErrorListener =
            new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.d("onError", "what" + what + " " + "extra" + extra);
        }
    };

    private CameraEngine() {
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        CameraEngine.previewCallback = previewCallback;
    }

    private void initRecorder(String moviePath) {
        mediaRecorder = new MediaRecorder();

        camera.unlock();
        mediaRecorder.setCamera(camera);

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);

        mediaRecorder.setProfile(camcorderProfile);

        mediaRecorder.setOutputFile(moviePath);

        mediaRecorder.setOnErrorListener(mediaRecorderErrorListener);
        mediaRecorder.setOnInfoListener(mediaRecorderInfoListener);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CameraEngine getInstance() {
        if (instance == null) {
            synchronized (CameraEngine.class) {
                if (instance == null) {
                    instance = new CameraEngine();
                }
            }
        }

        return instance;
    }

    private static Camera camera = null;

    public void openCamera() {
        if (camera == null) {
            try {
                camera = Camera.open(cameraId);
                setDefaultParameters();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean openCamera(int Id) {
        if (camera == null) {
            try {
                camera = Camera.open(Id);
                cameraId = Id;
                setDefaultParameters();
                return true;
            } catch (RuntimeException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    public void resumeCamera() {
        openCamera();
    }

    public void setParameters(Camera.Parameters parameters) {
        camera.setParameters(parameters);
    }

    public Camera.Parameters getParameters() {
        if (camera != null) {
            return camera.getParameters();
        }
        return null;
    }

    public void switchCamera() {
        releaseCamera();
        cameraId = cameraId == CAMERA_BACK ? CAMERA_FRONT : CAMERA_BACK;
        openCamera(cameraId);
        startPreview(surfaceTexture);
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        if (camera != null) {
            try {
                setFocusMode();
                setPreviewSize();
                setPictureSize();
                setPreviewFpsRange();
                camera.setPreviewTexture(surfaceTexture);
//                cameraCallbackBuffer1 = new byte[getYUVBufferSize(previewWidth, previewHeight)];
//                cameraCallbackBuffer2 = new byte[getYUVBufferSize(previewWidth, previewHeight)];
//                camera.addCallbackBuffer(cameraCallbackBuffer1);
//                camera.addCallbackBuffer(cameraCallbackBuffer2);
//                camera.setPreviewCallbackWithBuffer(previewCallback);
                camera.setPreviewCallback(previewCallback);
//                if (isFrontCamera()) {
//                    camera.setDisplayOrientation(180);
//                    setRotation(180);
//                }
                CameraEngine.surfaceTexture = surfaceTexture;
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isFrontCamera() {
        return cameraId == CAMERA_FRONT;
    }

    public void startPreview() {
        if (camera != null) {
            setFocusMode();
            setPreviewSize();
            setPictureSize();
            setPreviewFpsRange();
            camera.startPreview();
        }
    }

    public static void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
        }
    }

    public void takePicture(Camera.PictureCallback jpegCallback) {
        if (camera != null) {
            camera.takePicture(shutterCallback, null, jpegCallback);
        }
    }

    private String moviePath;

    public void startRecord(String moviePath) {
        this.moviePath = moviePath;
        initRecorder(moviePath);

        mediaRecorder.start();
        camera.setPreviewCallback(previewCallback);
    }

    public void stopRecord(RecordStatusCallback recordStatusCallback) {
        this.recordStatusCallback = recordStatusCallback;
        mediaRecorder.stop();

        mediaRecorder.release();
        mediaRecorder = null;

        recordStatusCallback.recordFinish(moviePath);
    }

    private RecordStatusCallback recordStatusCallback;

    public interface RecordStatusCallback {
        void recordFinish(String moviePath);
    }

    private void setDefaultParameters() {
        Camera.Parameters parameters = camera.getParameters();

        List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
        if (supportedPreviewFormats != null && supportedPreviewFormats.size() > 0) {
            if (supportedPreviewFormats.contains(ImageFormat.YV12)) {
                parameters.setPreviewFormat(ImageFormat.YV12);

                System.out.println("setPreviewFormat YV12");
            }
        }

        List<Integer> supportedPictureFormat = parameters.getSupportedPictureFormats();
        if (supportedPictureFormat != null && supportedPictureFormat.size() > 0) {
            if (supportedPictureFormat.contains(ImageFormat.JPEG)) {
                parameters.setPictureFormat(ImageFormat.JPEG);

                System.out.println("setPictureFormat JPEG");
            }
        }

        camera.setParameters(parameters);
    }

    private void setFocusMode() {
        Camera.Parameters parameters = camera.getParameters();

        List<String> supportedFocusModes = parameters.getSupportedFocusModes();

        if (supportedFocusModes != null ) {
            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                camera.cancelAutoFocus();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                camera.setParameters(parameters);
                System.out.println("setFocusMode FOCUS_MODE_CONTINUOUS_VIDEO");
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(parameters);
                System.out.println("setFocusMode FOCUS_MODE_CONTINUOUS_PICTURE");
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // TODO focus strategy
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                camera.setParameters(parameters);
                System.out.println("setFocusMode FOCUS_MODE_AUTO");
            } else {
                // TODO focus strategy
                System.out.println("setFocusMode NONE");
            }
        }
    }

    private void setPreviewSize() {
        Camera.Parameters parameters = camera.getParameters();

        Point displaySize = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getRealSize(displaySize);
        if (displaySize.x < displaySize.y) {
            displaySize = new Point(displaySize.y, displaySize.x);
        }

        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> candidateSize = new ArrayList<>();

        Camera.Size applySize = null;
        if (supportedPreviewSizes != null && supportedPreviewSizes.size() > 0) {
            for (Camera.Size size :
                    supportedPreviewSizes) {
                if ((size.width == displaySize.x) && (size.height == displaySize.y)) {
                    applySize = size;
                    break;
                }
                float eps = Math.abs(displaySize.x / (float) displaySize.y - size.width / (float) size.height);
                if (eps < 0.1) {
                    candidateSize.add(size);
                }
            }
        }

        if (applySize == null) {
            if (candidateSize != null && candidateSize.size() > 0) {
                Camera.Size minSize = candidateSize.get(0);
                for (Camera.Size size :
                        candidateSize) {
//                    if (size.width == 1280 && size.height == 720) {
//                        minSize = size;
//                        break;
//                    }
                    minSize = size.width < minSize.width ? size : minSize;
                }
                applySize = minSize;
            }
        }

        previewWidth = applySize.width;
        previewHeight = applySize.height;
        if (displaySize.x < displaySize.y) {
            previewWidth = applySize.height;
            previewHeight = applySize.width;

        }
        parameters.setPreviewSize(previewWidth, previewHeight);
        System.out.println("setPreviewSize " + applySize.width + "x" + applySize.height);
        camera.setParameters(parameters);
    }

    private void setPictureSize() {
        Camera.Parameters parameters = camera.getParameters();

        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        Camera.Size applySize = null;
        if (supportedPictureSizes != null && supportedPictureSizes.size() > 0) {
            Camera.Size maxSize = supportedPictureSizes.get(0);
            for (Camera.Size size :
                    supportedPictureSizes) {
                maxSize = size.width*size.height > maxSize.width*maxSize.height ? size : maxSize;
            }

            applySize = maxSize;
        }

        if (applySize != null) {
            parameters.setPictureSize(applySize.width, applySize.height);
            System.out.println("setPictureSize " + applySize.width + "x" + applySize.height);
            camera.setParameters(parameters);
        }
    }

    private void setPreviewFpsRange() {
        Camera.Parameters parameters = camera.getParameters();

        List<int[]> supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();
        int[] applyRange = null;
        if (supportedPreviewFpsRange != null && supportedPreviewFpsRange.size() > 0) {
            for (int[] fpsRange :
                    supportedPreviewFpsRange) {
                int min = fpsRange[0];
                int max = fpsRange[1];

                if ((min == 30000) && (max == 30000)) {
                    applyRange = fpsRange;
                    break;
                }
            }
        }

        if (applyRange != null) {
            parameters.setPreviewFpsRange(applyRange[0],applyRange[0]);
            System.out.println("setPreviewFpsRange " + applyRange[0] + " " + applyRange[0]);
            camera.setParameters(parameters);
        }
    }

    private void setRotation(int rotation) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(rotation);
        camera.setParameters(parameters);
    }

//    private class CameraPreviewCallback implements Camera.PreviewCallback {
//
//        @Override
//        public void onPreviewFrame(byte[] data, Camera camera) {
//            previewCallback.previewCallback(data, previewWidth, previewHeight);
//
//            camera.addCallbackBuffer(data);
//        }
//    }
//
//    public interface PreviewCallback {
//        void previewCallback(byte[] buffer, int width, int height);
//    }
//
//    private PreviewCallback previewCallback;
//
//    public void setPreviewCallback(PreviewCallback previewCallback) {
//        this.previewCallback = previewCallback;
//    }

    public static int getYUVBufferSize(int width, int height) {
        // stride = ALIGN(width, 16)
        int stride = (int)Math.ceil(width / 16.0) * 16;

        // y_size = stride * height
        int y_size = stride * height;

        // c_stride = ALIGN(stride/2, 16)
        int c_stride = (int)Math.ceil(width / 32.0) * 16;

        // c_size = c_stride * height/2
        int c_size = c_stride * height / 2;

        // size = y_size + c_size * 2
        return y_size + c_size * 2;
    }
}
