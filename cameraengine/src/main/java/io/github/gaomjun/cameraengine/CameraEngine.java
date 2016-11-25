package io.github.gaomjun.cameraengine;

import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Created by qq on 18/11/2016.
 */

public class CameraEngine {
    private volatile static CameraEngine instance = null;
    private static int cameraId = 0;
    private static SurfaceTexture surfaceTexture;
    public static int previewWidth;
    public static int previewHeight;
    public static Camera.PreviewCallback previewCallback;

    private CameraEngine() {

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

    public static boolean openCamera() {
        if (camera == null) {
            try {
                camera = Camera.open(cameraId);
                setDefaultParameters();
                return true;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean openCamera(int Id) {
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

    public static void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
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

    public static void switchCamera() {
        releaseCamera();
        cameraId = cameraId == 0 ? 1 : 0;
        openCamera(cameraId);
        startPreview(surfaceTexture);
    }

    public static void startPreview(SurfaceTexture surfaceTexture) {
        if (camera != null) {
            try {
                camera.setPreviewTexture(surfaceTexture);
                camera.setPreviewCallback(previewCallback);
                CameraEngine.surfaceTexture = surfaceTexture;
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isFrontCamera() {
        return cameraId == 1 ? true : false;
    }

    public static void startPreview() {
        if (camera != null) {
            camera.startPreview();
        }
    }

    public static void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
        }
    }

    public static void takePicture(Camera.PictureCallback jpegCallback) {
        if (camera != null) {
            camera.takePicture(null, null, jpegCallback);
        }
    }

    private static void setDefaultParameters() {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        Camera.Size previewSize = getLargePreviewSize();
        previewWidth = previewSize.width;
        previewHeight = previewSize.height;
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        parameters.setPreviewFormat(PixelFormat.RGB_888);

        Camera.Size pictureSize = getLargePictureSize();
        parameters.setPictureSize(pictureSize.width, pictureSize.height);

        parameters.setRotation(90);

        camera.setParameters(parameters);
    }

    private static Camera.Size getLargePictureSize() {
        if (camera != null) {
            List<Camera.Size> supportedPictureSizes = camera.getParameters().getSupportedPictureSizes();
            Camera.Size size_t = supportedPictureSizes.get(0);
            for (Camera.Size size :
                    supportedPictureSizes) {
                float scale = (float)(size.height) / size.width;
                if(size_t.width < size.width && scale < 0.6f && scale > 0.5f) {
                    size_t = size;
                }
            }
            return size_t;
        }
        return null;
    }

    private static Camera.Size getLargePreviewSize() {
        if (camera != null) {
            List<Camera.Size> supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
            Camera.Size size_t = supportedPreviewSizes.get(0);
            for (Camera.Size size :
                    supportedPreviewSizes) {
                if (size_t.width < size.width) {
                    size_t = size;
                }
            }
            return size_t;
        }
        return null;
    }
}
