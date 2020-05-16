package io.github.gaomjun.cameraengine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.gaomjun.yuvdecoder.YUVDecoder;

/**
 * Created by qq on 23/12/2016.
 */

public class CameraEngine2 {

    public Context context;
    private CameraManager cameraManager;
    private AutoFitTextureView autoFitTextureView;
    private Point displaySize = new Point();

    private volatile static CameraEngine2 instance = null;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            CameraEngine2.this.cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };
    private HandlerThread cameraThread = new HandlerThread("cameraThread");
    private Handler cameraHandler;
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }

        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        }

        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
        }

        @Override
        public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request, Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
        }
    };
    private CaptureRequest.Builder previewRequestBuilder;
    public int previewWidth;
    public int previewHeight;
    private ImageReader previewCallbackImageReader;

    private CameraEngine2() {

    }

    public static CameraEngine2 getInstance() {
        if (instance == null) {
            synchronized (CameraEngine.class) {
                if (instance == null) {
                    instance = new CameraEngine2();
                }
            }
        }

        return instance;
    }

    public void initCamera() {
        ((Activity) context).getWindowManager().getDefaultDisplay().getRealSize(displaySize);
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());

        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            if (cameraIdList != null && cameraIdList.length > 0) {
                for (String cameraId : cameraIdList) {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                    if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                        // default camera is back
                        this.cameraId = cameraId;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public void openCamera(AutoFitTextureView autoFitTextureView) {
        this.autoFitTextureView = autoFitTextureView;

        try {
            if (cameraId != null) {
                cameraManager.openCamera(cameraId, cameraDeviceStateCallback, null);
            } else {
                System.out.println("no back camera");
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void releaseCamera() {

    }

    public void switchCamera() {

    }

    public void startPreview(SurfaceTexture surfaceTexture) {

    }

    public static boolean isFrontCamera() {
        return false;
    }

    public void startPreview() {
        if (cameraDevice != null && autoFitTextureView != null) {
            setPreviewSize();
            setPreviewRotation();
            setPreviewCallback();

            Surface previewSurface = new Surface(autoFitTextureView.getSurfaceTexture());
            List<Surface> cameraOutputs = Arrays.asList(previewSurface, previewCallbackImageReader.getSurface());

            try {
                previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//                previewRequestBuilder.addTarget(previewSurface);
                previewRequestBuilder.addTarget(previewCallbackImageReader.getSurface());
                setFocusMode();

                cameraDevice.createCaptureSession(cameraOutputs, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(CameraCaptureSession session) {
                        try {
                            session.setRepeatingRequest(previewRequestBuilder.build(), captureCallback, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(CameraCaptureSession session) {

                    }
                }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPreviewCallback() {
        previewCallbackImageReader = ImageReader.newInstance(previewWidth, previewHeight, ImageFormat.YUV_420_888, 2);
        previewCallbackImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();
                if (image != null) {
                    ByteBuffer bufferY = image.getPlanes()[0].getBuffer();
                    ByteBuffer bufferU = image.getPlanes()[1].getBuffer();
                    ByteBuffer bufferV = image.getPlanes()[2].getBuffer();

                    byte[] dataY = new byte[bufferY.remaining()];
                    byte[] dataU = new byte[bufferU.remaining()];
                    byte[] dataV = new byte[bufferV.remaining()];

                    bufferY.get(dataY);
                    bufferU.get(dataU);
                    bufferV.get(dataV);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    try {
                        outputStream.write(dataY);
                        for (int i=0; i < bufferV.remaining(); i++) {
                            outputStream.write(dataU[i]);
                            outputStream.write(dataV[i]);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    byte[] yuv = outputStream.toByteArray();

                    byte[] rgb = new byte[previewWidth*previewHeight];
//                    YUVDecoder.YUVToARGB(yuv, previewWidth, previewHeight, rgb);

//                    final Bitmap imageBitmap = BitmapFactory.decodeByteArray(rgb., 0, rgb.length);

//                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    Log.d("onImageAvailable ", image.getWidth() + "x" + image.getHeight());
                    image.close();
                }
            }
        }, cameraHandler);
    }

    public void stopPreview() {
    }

    public void takePicture() {

    }

    public void startRecord() {

    }

    public void stopRecord() {

    }

    private void setPreviewSize() {
        if (cameraManager != null && cameraId != null) {
            try {
                StreamConfigurationMap streamConfigurationMap = cameraManager.getCameraCharacteristics(cameraId)
                                                                             .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] outputSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);

                if (outputSizes != null && outputSizes.length > 0) {
                    List<Size> candidateSize = new ArrayList<>();
                    for (Size size : outputSizes) {
                        float eps = Math.abs(size.getWidth() / (float) size.getHeight() - displaySize.x / (float) displaySize.y);
                        float eps2 = Math.abs(size.getWidth() / (float) size.getHeight() - displaySize.y/(float)displaySize.x);
                        if (eps < .1 || eps2 < .1) {
                            candidateSize.add(size);
                        }
                    }

                    if (candidateSize.size() > 0) {
                        Size minSize = candidateSize.get(0);
                        for (Size size :
                                candidateSize) {
                            minSize = size.getWidth() < minSize.getWidth() ? size : minSize;
                        }

                        previewWidth = minSize.getWidth();
                        previewHeight = minSize.getHeight();

                        autoFitTextureView.getSurfaceTexture().setDefaultBufferSize(previewWidth, previewHeight);
                        System.out.println("setPreviewSize " + previewWidth + "x" + previewHeight);
                    }
                }

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPreviewRotation() {
        if (displaySize.x > displaySize.y) { // landscape

            int width = displaySize.x;
            int height = displaySize.y;

            Matrix matrix = new Matrix();

            RectF viewRect = new RectF(0, 0, width, height);

            RectF bufferRect = new RectF(0, 0, previewHeight, previewWidth);

            float centerX = viewRect.centerX();
            float centerY = viewRect.centerY();

            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());

            float scale = Math.max(
                    (float) height / previewHeight,
                    (float) width / previewWidth);

            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            matrix.postScale(scale, scale, centerX, centerY);

            matrix.postRotate(-90, centerX, centerY);

            autoFitTextureView.setTransform(matrix);
        } else { // portrait

        }
    }

    private void setFocusMode() {
        if (previewRequestBuilder != null) {
            try {
                int[] availableFocusMode = cameraManager.getCameraCharacteristics(cameraId)
                                                        .get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                List<Integer> availableFocusModeList = new ArrayList<Integer>();

                for (int focusMode : availableFocusMode) {
                    availableFocusModeList.add(focusMode);
                }

                if (availableFocusModeList.contains(CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO)) {
                    previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                    System.out.println("setFocusMode " + "CONTROL_AF_MODE_CONTINUOUS_VIDEO");
                } else if (availableFocusModeList.contains(CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
                    previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    System.out.println("setFocusMode " + "CONTROL_AF_MODE_CONTINUOUS_PICTURE");
                } else if (availableFocusModeList.contains(CameraCharacteristics.CONTROL_AF_MODE_AUTO)) {
                    previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                    System.out.println("setFocusMode " + "CONTROL_AF_MODE_AUTO");
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
