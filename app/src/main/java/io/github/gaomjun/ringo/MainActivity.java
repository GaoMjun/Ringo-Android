package io.github.gaomjun.ringo;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.BaseViewAnimator;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.androidanimations.library.rotating.RotateAnimator;
import com.daimajia.androidanimations.library.translation.TranslationXAnimation;
import com.daimajia.androidanimations.library.translation.TranslationYAnimation;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.github.gaomjun.blecommunication.BLECommunication.BLEDriven;
import io.github.gaomjun.blecommunication.BLECommunication.Message.GimbalMobileBLEProtocol;
import io.github.gaomjun.blecommunication.BLECommunication.Message.RecvMessage;
import io.github.gaomjun.blecommunication.BLECommunication.Message.SendMessage;
import io.github.gaomjun.cameraengine.CameraEngine;
import io.github.gaomjun.cmttracker.CMTTracker;
import io.github.gaomjun.cvcamera.CVCamera;
import io.github.gaomjun.gallary.gallary_grid.ui.GallaryGridActivity;
import io.github.gaomjun.gl.GLTextureView;
import io.github.gaomjun.gl.OffScreenRenderer;
import io.github.gaomjun.motionorientation.MotionOrientation;
import io.github.gaomjun.ringo.BluetoothDevicesList.Adapter.BluetoothDevicesListAdapter;
import io.github.gaomjun.ringo.BluetoothDevicesList.DataSource.BluetoothDevicesListDataSource;
import io.github.gaomjun.timelabel.TimeLabel;
import io.github.gaomjun.utils.TypeConversion.TypeConversion;
import pub.devrel.easypermissions.EasyPermissions;

import static io.github.gaomjun.motionorientation.MotionOrientation.getDEVICE_ORIENTATION_LANDSCAPELEFT;
import static io.github.gaomjun.motionorientation.MotionOrientation.getDEVICE_ORIENTATION_LANDSCAPERIGHT;
import static io.github.gaomjun.motionorientation.MotionOrientation.getDEVICE_ORIENTATION_PORTRAIT;
import static io.github.gaomjun.motionorientation.MotionOrientation.getDEVICE_ORIENTATION_UPSIDEDOWN;

public class MainActivity extends AppCompatActivity implements
        CVCamera.FrameCallback,
        CVCamera.FrameListener,
        EasyPermissions.PermissionCallbacks,
        MotionOrientation.DeviceOrientationListener,
        CameraEngine.DetectFaceListener {
    private static final int REQUEST_BLUETOOTH_ON_CODE = 11;
    private TimeLabel timeLabel = new TimeLabel();
    private BLEDriven bleDriven = null;
    private SendMessage sendMessage = SendMessage.getInstance();

    private BluetoothDevicesListAdapter bluetoothDevicesListAdapter;
    private BluetoothDevicesListDataSource bluetoothDevicesListDataSource = new BluetoothDevicesListDataSource();
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private BluetoothDevice bluetoothDevice;

    private HandlerThread trackingThread = null;
    private Handler trackingThreadHandler = null;

    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private int SCALE;

    private Point startPoint = new Point();
    private Point endPoint = new Point();

    private ViewUtils trackingBoxUtils;
    private CMTTracker cmtTracker = null;
    private CVCamera cvCamera = null;
    private CameraEngine cameraEngine = null;

    @BindView(R2.id.testImageView) ImageView testImageView;
    @BindView(R2.id.bluetooth_devices_list_view) RelativeLayout bluetooth_devices_list_view;
    @BindView(R2.id.trackingBox) View trackingBox;
    @BindView(R2.id.recordTimeLabel) TextView recordTimeLabel;
    @BindView(R2.id.bluetooth_devices_list) RecyclerView bluetooth_devices_list;

    @BindView(R2.id.cameraView) GLTextureView cameraView;

    @BindView(R2.id.leftbar) LinearLayout leftbar;
    @BindView(R2.id.iv_switch_camera_mode) ImageView iv_switch_camera_mode;
    @BindView(R2.id.iv_capture) ImageView iv_capture;
    @BindView(R2.id.iv_switch_camera) ImageView iv_switch_camera;

    @BindView(R2.id.rightbar) LinearLayout rightbar;
    @BindView(R2.id.iv_ble) ImageView iv_ble;
    @BindView(R2.id.iv_tracking_status) ImageView iv_tracking_status;
    @BindView(R2.id.iv_album) ImageView iv_album;

    @BindView(R2.id.bluetooth_devices_list_close) ImageView bluetooth_devices_list_close;

    @OnClick({R2.id.iv_switch_camera_mode,
            R2.id.iv_capture,
            R2.id.iv_switch_camera,
            R2.id.iv_ble,
            R2.id.iv_tracking_status,
            R2.id.iv_album,
            R2.id.bluetooth_devices_list_close})
    public void clickAction(View v) {
        switch (v.getId()) {
            case R.id.iv_capture:
            {
                Integer tag = (Integer) iv_capture.getTag();

                if ((tag == null) || tag == R.drawable.iv_capture) {
                    YoYo.with(Techniques.Flash)
                            .duration(200)
                            .playOn(cameraView);
                    takePicture();

                } else {
                    if (!cameraView.getRecording()) {
                        // start record
                        if (startRecord()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "start record", Toast.LENGTH_SHORT).show();
                                }
                            });
                            iv_capture.setSelected(!iv_capture.isSelected());
                            {
                                // disable some button
                                iv_switch_camera_mode.setEnabled(false);
                                iv_switch_camera.setEnabled(false);
                                iv_album.setEnabled(false);
                            }
                        }

                    } else {
                        // stop record
                        stopRecord();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "stop record", Toast.LENGTH_SHORT).show();
                            }
                        });
                        iv_capture.setSelected(!iv_capture.isSelected());
                        {
                            // enable disabled button
                            iv_switch_camera_mode.setEnabled(true);
                            iv_switch_camera.setEnabled(true);
                            iv_album.setEnabled(true);
                        }
                    }
                }
            }

            break;
            case R.id.iv_switch_camera:
                //TODO animation
//                    cameraEngine.stopPreview();
//                    YoYo.with(Techniques.FlipInY)
//                            .duration(200)
//                            .playOn(cameraView);

                cameraEngine.switchCamera();
                canTrackerInit = false;
                startTracking = false;
                sendMessage.setTrackingQuailty(GimbalMobileBLEProtocol.TRACKING_QUALITY_WEAK);

                break;
            case R.id.bluetooth_devices_list_close:
                bluetooth_devices_list_view.setVisibility(View.GONE);
                bleDriven.stopScanDevices();
                break;
            case R.id.iv_ble:
                // check bluetooth is available
                if (!bleDriven.bluetoothIsAvailable) {
                    Log.d("R.id.iv_ble", "bluetooth is not available");
                    //TODO request opening on bluetooth
                    startActivityForResult(
                            new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_BLUETOOTH_ON_CODE);
                    return;
                }

                if (bluetooth_devices_list_view.getVisibility() == View.GONE) {
                    bluetooth_devices_list_view.setVisibility(View.VISIBLE);
                    datasourceChanged(new ArrayList<BluetoothDevice>(), bluetoothDevice);
                    bleDriven.scanDevices();
                } else {
                    bluetooth_devices_list_view.setVisibility(View.GONE);
                    bleDriven.stopScanDevices();
                }

                break;
            case R.id.iv_tracking_status:
                if (canSwitchTrackingStatus) {
                    canSwitchTrackingStatus = false;

                    iv_tracking_status.setSelected(!iv_tracking_status.isSelected());
                    if (iv_tracking_status.isSelected()) {
//                        Log.d("iv_tracking_status", "selected");
                        sendMessage.setTrackingFlag(GimbalMobileBLEProtocol.TRACKING_FLAG_ON);
                        sendMessage.setTrackingQuailty(GimbalMobileBLEProtocol.TRACKING_QUALITY_WEAK);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                canSwitchTrackingStatus = true;
                            }
                        }, 500);
                    } else {
//                        Log.d("iv_tracking_status", "no selected");
                        sendMessage.setTrackingFlag(GimbalMobileBLEProtocol.TRACKING_FLAG_OFF);
                        sendMessage.setTrackingQuailty(GimbalMobileBLEProtocol.TRACKING_QUALITY_WEAK);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                canSwitchTrackingStatus = true;
                            }
                        }, 500);
                        canTrackerInit = false;
                        startTracking = false;
                        if (trackingBox.getVisibility() != View.GONE) {
                            trackingBox.setVisibility(View.GONE);
                        }

                    }
                }
                break;
            case R.id.iv_switch_camera_mode:
            {
                Integer tag = (Integer) iv_switch_camera_mode.getTag();
                if ((tag == null) || tag == R.drawable.camera_mode_photo) {
                    iv_switch_camera_mode.setImageResource(R.drawable.camera_mode_video);
                    iv_switch_camera_mode.setTag(R.drawable.camera_mode_video);
                    iv_switch_camera_mode.setScaleX((float) 0.8);
                    iv_switch_camera_mode.setScaleY((float) 0.8);

                    iv_capture.setImageResource(R.drawable.iv_record);
                    iv_capture.setTag(R.drawable.iv_record);
                } else {
                    iv_switch_camera_mode.setImageResource(R.drawable.camera_mode_photo);
                    iv_switch_camera_mode.setTag(R.drawable.camera_mode_photo);
                    iv_switch_camera_mode.setScaleX((float) 0.9);
                    iv_switch_camera_mode.setScaleY((float) 0.9);

                    iv_capture.setImageResource(R.drawable.iv_capture);
                    iv_capture.setTag(R.drawable.iv_capture);
                }
            }
            break;
            case R.id.iv_album:
                startActivity(new Intent(MainActivity.this, GallaryGridActivity.class));
                break;
        }
    }

    @OnTouch(R2.id.activity_main)
    public boolean touchAction(View v, MotionEvent event) {
        if (bluetooth_devices_list.getVisibility() == View.VISIBLE) {
            bluetooth_devices_list_close.performClick();
        }
        if (canTracking == false) return true;

        double x, y, w, h;

        final Point point = new Point(event.getX(), event.getY());
//        Log.d("OnTouch", point.toString());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startPoint.x = point.x;
                startPoint.y = point.y;

//                    Log.d("MotionEvent", "touch start" + startPoint.toString());

            {
                canTrackerInit = false;
                startTracking = false;
                trackingBox.setVisibility(View.GONE);
            }
            break;

            case MotionEvent.ACTION_MOVE:
                endPoint.x = point.x;
                endPoint.y = point.y;

                x = startPoint.x;
                y = startPoint.y;
                w = startPoint.x - endPoint.x;
                h = startPoint.y - endPoint.y;

                if (w < 0) {
                    w = -w;
                } else {
                    x = endPoint.x;
                }

                if (h < 0) {
                    h = -h;
                } else {
                    y = endPoint.y;
                }

                trackingBox.setVisibility(View.VISIBLE);
                trackingBoxUtils.setX((int) x, 0);
                trackingBoxUtils.setY((int) y, 0);
                trackingBoxUtils.setWidth((int) w, 0);
                trackingBoxUtils.setHeight((int) h, 0);

//                    Log.d("MotionEvent", "touch move" + endPoint.toString());
                break;
            case MotionEvent.ACTION_UP:
                endPoint.x = point.x;
                endPoint.y = point.y;

//                double x = startPoint.x;
//                double y = startPoint.y;
                w = startPoint.x - endPoint.x;
                h = startPoint.y - endPoint.y;

                if (w < 0) {
                    w = -w;
                } else {
                    x = endPoint.x;
                }

                if (h < 0) {
                    h = -h;
                } else {
                    y = endPoint.y;
                }

                if (w > 100 && h > 100) {
                    canTrackerInit = true;
                    // save box
                    boxWidth = w;
                    boxHeight = h;
                } else {
                    canTrackerInit = false;
                    startTracking = false;
                    sendMessage.setTrackingQuailty(GimbalMobileBLEProtocol.TRACKING_QUALITY_WEAK);
                    Log.d("OnTouch", "selected box is too small");
                }
                trackingBoxUtils.setRect(0, 0, 0, 0, 0);
                trackingBox.setVisibility(View.GONE);
//                    Log.d("MotionEvent", "touch end" + endPoint.toString());
                break;
        }

        return true;
    }

    private boolean canSwitchTrackingStatus = true;

    private boolean canCapture = true;
    private boolean canRecord = true;
    private boolean canSwitch = true;
    private boolean allPermissionGranted = false;

    private void takePicture() {
        cameraEngine.takePicture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                Log.d("onPictureTaken", "takePicture");
                camera.startPreview();
                if (data != null) {
                    Camera.Size pictureSize = camera.getParameters().getPictureSize();
                    OffScreenRenderer.render(getApplicationContext(), data, pictureSize.width, pictureSize.height, new OffScreenRenderer.Companion.Callback() {
                        @Override
                        public void renderFinish(@NotNull Bitmap bitmap) {
                            Log.d("renderFinish", "renderFinish");
                            Matrix matrix = new Matrix();
                            if (cameraEngine.isFrontCamera()) {
                                matrix.postScale(-1.0f, 1.0f);
                            }
                            int deviceOrientation = MotionOrientation.getDEVICE_ORIENTATION();
//                            Log.d("takePicture", deviceOrientation + "");
                            switch (deviceOrientation) {
                                case 1: {
                                    matrix.postRotate(270.0F);
                                    break;
                                }

                                case 2: {
                                    matrix.postRotate(90.0F);
                                    break;
                                }

                                case 3: {
                                    matrix.postRotate(180.0F);
                                    break;
                                }

                                case 4: {
                                    break;
                                }

                                default: {
                                    matrix.postRotate(270.0F);
                                    break;
                                }
                            }
                            Bitmap bitmapFix = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            savePhotoToAlbum(bitmapFix);
                        }
                    }, null);
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            savePhotoToAlbum(data);
//                        }
//                    }).start();
                }
            }
        });
    }

    private void stopRecord() {
        timeLabel.stop();
        recordTimeLabel.setVisibility(View.GONE);

        cameraView.stopRecord(new GLTextureView.RecordStatusCallback() {
            @Override
            public void recordFinish(@org.jetbrains.annotations.Nullable String moviePath) {
                Log.d("recordFinish", moviePath);
                MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + moviePath)));
            }
        }, null);
    }

    private boolean startRecord() {
        // check free space
        final int seconds = (int) ((getFreeSpace() - 100) / 1.5);
        final String maxTimeString = TimeLabel.secondsToTimeString(seconds);

        if (seconds <= 3) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "剩余空间不足", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }

        // start record timer
        recordTimeLabel.setVisibility(View.VISIBLE);
        timeLabel.setTimeChangedCallback(new TimeLabel.TimeChangedCallback() {
            @Override
            public void timeChanged(final String timeString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recordTimeLabel.setText(timeString + "|" + maxTimeString);
                    }
                });
                if (maxTimeString.equals(timeString)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_capture.performClick();
                        }
                    });
                }
            }
        });
        timeLabel.start();

        String ringoDirectory = ringoDirectory();
        if (ringoDirectory == null) return false;

        String currentTimeString = new SimpleDateFormat("yyyyMMddHHmmSS", Locale.US).format(new Date());
        String moviePath = ringoDirectory + "/" + currentTimeString + ".mp4";
        cameraView.startRecord(moviePath);

        return true;
    }

    private long getFreeSpace() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getAvailableBytes();
        return bytesAvailable / 1048576;
    }

    private ProgressBar progressBar_connecting_ble_device;
    private BluetoothDevicesListAdapter.CellClickCallback cellOnClickListener =
            new BluetoothDevicesListAdapter.CellClickCallback() {
        @Override
        public void cellOnClick(ProgressBar progressBar_connecting_ble_device, int position) {
            Log.d("cellOnClick", "" + position);

            if (bluetoothDevice != null) {
                if (bluetoothDevice.getAddress().equals(bluetoothDeviceList.get(position).getAddress())) {
                    return;
                } else {
                    bleDriven.disconnectDevice();
                    bluetoothDevice = null;
                }
            }
            bluetoothDevice = bluetoothDeviceList.get(position);
            Log.d("connectToDevice", bluetoothDevice.getName());
            bleDriven.connectToDevice(bluetoothDevice.getAddress());
            MainActivity.this.progressBar_connecting_ble_device = progressBar_connecting_ble_device;
            progressBar_connecting_ble_device.setVisibility(View.VISIBLE);
        }
    };
    private boolean canTracking = false;

    private void savePhotoToAlbum(final Bitmap bitmap) {
        if (bitmap != null) {
            String ringoDirectory = ringoDirectory();
            if (ringoDirectory == null) return;

            File file = new File(ringoDirectory + "/", System.currentTimeMillis() + ".jpg");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                bitmap.recycle();

                fileOutputStream.flush();
                fileOutputStream.close();

                Log.d("onPictureTaken", "take picture success " + file.toString());
                this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + file.toString())));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "take picture success",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void savePhotoToAlbum(final byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        savePhotoToAlbum(bitmap);
    }

    @Nullable
    private String ringoDirectory() {
        File ringoDirectory = new File(Environment.getExternalStorageDirectory() + "/" +
                Environment.DIRECTORY_DCIM + "/", "Ringo");
        if (!ringoDirectory.exists()) {
            if (!ringoDirectory.mkdir()) {
                //TODO
                Log.w("ringoDirectory", "Ringo mkdir error");
                return null;

            } else {
                return ringoDirectory.getAbsolutePath();
            }
        }

        if (!ringoDirectory.isDirectory()) {
            Log.w("ringoDirectory", "Ringo is not directory");
            return null;
        }

        return ringoDirectory.getAbsolutePath();
    }

    private double boxWidth = 0;
    private double boxHeight = 0;
    private boolean canTrackerInit = false;
    private boolean startTracking = false;

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    private boolean checkPermission() {
        String[] perms = { Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            allPermissionGranted = true;
            return true;
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this, "this app need these permission",
                    124, perms);
        }

        return false;
    }

    @Override
    public void deviceOrientationChanged(int orientaion) {

    }

    private float originX;
    private float originY;
    private float originY2;

    @Override
    public void deviceOrientationChangedFromTo(int from, int to) {

        float fromDegree;
        float toDegree;

        switch (from) {
            case 1:
                fromDegree = -90;
                break;
            case 2:
                fromDegree = 90;
                break;
            case 3:
                fromDegree = 0;
                break;
            case 4:
                fromDegree = 180;
                break;
            default:
                fromDegree = 0;
                break;
        }

        switch (to) {
            case 1:
                cameraView.setOrientation(to);
                toDegree = -90;
                if (originX == leftbar.getX()) {
                    float distance = rightbar.getX();
                    YoYo.with(new TranslationXAnimation(0, distance)).duration(0).playOn(leftbar);
                    YoYo.with(new TranslationXAnimation(0, -distance)).duration(0).playOn(rightbar);
                    leftbar.setBackgroundResource(R.drawable.rightbar_bg_mask);
                    rightbar.setBackgroundResource(R.drawable.leftbar_bg_mask);
                }

                if (originY == iv_ble.getY()) {
                    float distanceY = iv_album.getY() - iv_ble.getY();
                    YoYo.with(new TranslationYAnimation(0, distanceY)).duration(0).playOn(iv_ble);
                    YoYo.with(new TranslationYAnimation(0, -distanceY)).duration(0).playOn(iv_album);

                    float distanceY2 = iv_switch_camera.getY() - iv_switch_camera_mode.getY();
                    YoYo.with(new TranslationYAnimation(0, distanceY2)).duration(0).playOn(iv_switch_camera_mode);
                    YoYo.with(new TranslationYAnimation(0, -distanceY2)).duration(0).playOn(iv_switch_camera);
                }

                break;
            case 2:
                cameraView.setOrientation(to);
                toDegree = 90;
                if (originX != leftbar.getX()) {
                    float distance = leftbar.getX();
                    YoYo.with(new TranslationXAnimation(distance, 0)).duration(0).playOn(leftbar);
                    YoYo.with(new TranslationXAnimation(-distance, 0)).duration(0).playOn(rightbar);
                    leftbar.setBackgroundResource(R.drawable.leftbar_bg_mask);
                    rightbar.setBackgroundResource(R.drawable.rightbar_bg_mask);
                }

                if (originY != iv_ble.getY()) {
                    float distanceY = iv_ble.getY() - iv_album.getY();
                    YoYo.with(new TranslationYAnimation(distanceY, 0)).duration(0).playOn(iv_ble);
                    YoYo.with(new TranslationYAnimation(-distanceY, 0)).duration(0).playOn(iv_album);

                    float distanceY2 = iv_switch_camera_mode.getY() - iv_switch_camera.getY();
                    YoYo.with(new TranslationYAnimation(distanceY2, 0)).duration(0).playOn(iv_switch_camera_mode);
                    YoYo.with(new TranslationYAnimation(-distanceY2, 0)).duration(0).playOn(iv_switch_camera);
                }

                break;
            case 3:
                cameraView.setOrientation(to);
                toDegree = 0;
                if (originX != leftbar.getX()) {
                    float distance = leftbar.getX();
                    YoYo.with(new TranslationXAnimation(distance, 0)).duration(0).playOn(leftbar);
                    YoYo.with(new TranslationXAnimation(-distance, 0)).duration(0).playOn(rightbar);
                    leftbar.setBackgroundResource(R.drawable.leftbar_bg_mask);
                    rightbar.setBackgroundResource(R.drawable.rightbar_bg_mask);
                }

                if (originY != iv_ble.getY()) {
                    float distanceY = iv_ble.getY() - iv_album.getY();
                    YoYo.with(new TranslationYAnimation(distanceY, 0)).duration(0).playOn(iv_ble);
                    YoYo.with(new TranslationYAnimation(-distanceY, 0)).duration(0).playOn(iv_album);

                    float distanceY2 = iv_switch_camera_mode.getY() - iv_switch_camera.getY();
                    YoYo.with(new TranslationYAnimation(distanceY2, 0)).duration(0).playOn(iv_switch_camera_mode);
                    YoYo.with(new TranslationYAnimation(-distanceY2, 0)).duration(0).playOn(iv_switch_camera);
                }

                break;
            case 4:
                cameraView.setOrientation(to);
                toDegree = 180;
                if (originX == leftbar.getX()) {
                    float distance = rightbar.getX();
                    YoYo.with(new TranslationXAnimation(0, distance)).duration(0).playOn(leftbar);
                    YoYo.with(new TranslationXAnimation(0, -distance)).duration(0).playOn(rightbar);

                    leftbar.setBackgroundResource(R.drawable.rightbar_bg_mask);
                    rightbar.setBackgroundResource(R.drawable.leftbar_bg_mask);
                }

                if (originY == iv_ble.getY()) {
                    float distanceY = iv_album.getY() - iv_ble.getY();
                    YoYo.with(new TranslationYAnimation(0, distanceY)).duration(0).playOn(iv_ble);
                    YoYo.with(new TranslationYAnimation(0, -distanceY)).duration(0).playOn(iv_album);

                    float distanceY2 = iv_switch_camera.getY() - iv_switch_camera_mode.getY();
                    YoYo.with(new TranslationYAnimation(0, distanceY2)).duration(0).playOn(iv_switch_camera_mode);
                    YoYo.with(new TranslationYAnimation(0, -distanceY2)).duration(0).playOn(iv_switch_camera);
                }

                break;
            default:
                toDegree = 0;
                if (originX != leftbar.getX()) {
                    float distance = leftbar.getX();
                    YoYo.with(new TranslationXAnimation(distance, 0)).duration(0).playOn(leftbar);
                    YoYo.with(new TranslationXAnimation(-distance, 0)).duration(0).playOn(rightbar);
                    leftbar.setBackgroundResource(R.drawable.leftbar_bg_mask);
                    rightbar.setBackgroundResource(R.drawable.rightbar_bg_mask);
                }

                if (originY != iv_ble.getY()) {
                    float distanceY = iv_ble.getY() - iv_album.getY();
                    YoYo.with(new TranslationYAnimation(distanceY, 0)).duration(0).playOn(iv_ble);
                    YoYo.with(new TranslationYAnimation(-distanceY, 0)).duration(0).playOn(iv_album);

                    float distanceY2 = iv_switch_camera_mode.getY() - iv_switch_camera.getY();
                    YoYo.with(new TranslationYAnimation(distanceY2, 0)).duration(0).playOn(iv_switch_camera_mode);
                    YoYo.with(new TranslationYAnimation(-distanceY2, 0)).duration(0).playOn(iv_switch_camera);
                }

                break;
        }

        View[] views = {iv_switch_camera_mode, iv_switch_camera, iv_ble, iv_album, bluetooth_devices_list_view};
        playAnimationOnMultiView(views, new RotateAnimator(fromDegree, toDegree), 100);
    }

    private void playAnimationOnMultiView(final View[] views, BaseViewAnimator animator, long duration) {
        for (View v : views) {
            YoYo.with(animator)
                    .duration(duration)
                    .playOn(v);
        }
    }

    @Override
    public void faceDetected(boolean detected) {
        if (detected) {
//            Log.d("faceDetected", "detected");
            cameraView.setBeautifiy(1);
        } else {
//            Log.d("faceDetected", "not detected");
            cameraView.setBeautifiy(0);
        }
    }

    private class RecvDataListener implements BLEDriven.RecvCallback {

        @Override
        public void onRecvData(RecvMessage recvMessage) {
//            Log.d("recv", recvMessage.getMessageHexString());

            byte[] command = recvMessage.getCommand();

            if (Arrays.equals(command, GimbalMobileBLEProtocol.REMOTECOMMAND_CAPTURE)) {
                //TODO
                //capture action
                if (canCapture) {
                    canCapture = false;
                    // start capture
                    Log.d("onRecvData", "capture action");
                    {
                        if (!cameraView.getRecording()) {

                            Integer tag = (Integer) iv_switch_camera_mode.getTag();
                            if ((tag != null) && (tag != R.drawable.camera_mode_photo)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        iv_switch_camera_mode.performClick();
                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                iv_capture.performClick();
                                            }
                                        }, 10);

                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        iv_capture.performClick();
                                    }
                                });
                            }

                        } else {
                            // while muxing, can not capture
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,  "while muxing, can not capture", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    sendMessage.setCommandBack(GimbalMobileBLEProtocol.COMMANDBACK_CAPTRUE_OK);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            canCapture = true;
                        }
                    }, 500);
                }

            } else if (Arrays.equals(command, GimbalMobileBLEProtocol.REMOTECOMMAND_RECORD)) {
                //TODO
                //record action
                if (canRecord) {
                    canRecord = false;
                    Log.d("onRecvData", "record action");
                    {
                        Integer tag = (Integer) iv_switch_camera_mode.getTag();
                        if ((tag == null) || (tag != R.drawable.camera_mode_video)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    iv_switch_camera_mode.performClick();
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            iv_capture.performClick();
                                        }
                                    }, 10);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    iv_capture.performClick();
                                }
                            });
                        }
                    }
                    sendMessage.setCommandBack(GimbalMobileBLEProtocol.COMMANDBACK_RECORD_OK);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            canRecord = true;
                        }
                    }, 500);
                }

            } else if (Arrays.equals(command, GimbalMobileBLEProtocol.REMOTECOMMAND_SWITCH)) {
                if (canSwitch) {
                    canSwitch = false;

                    Log.d("onRecvData", "swich camera action");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_switch_camera.performClick();
                        }
                    });
                    sendMessage.setCommandBack(GimbalMobileBLEProtocol.COMMANDBACK_SWITCH_OK);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            canSwitch = true;
                        }
                    }, 500);
                }

            } else if (Arrays.equals(command, GimbalMobileBLEProtocol.REMOTECOMMAND_CLEAR)){
                sendMessage.setCommandBack(GimbalMobileBLEProtocol.COMMADNBACK_CLEAR);
            }

            byte[] gimbalStatus = recvMessage.getGimbalStatus();

            if (Arrays.equals(gimbalStatus, GimbalMobileBLEProtocol.GIMBALSTATUS_RUN)) {
                // enable switch tracking status button
                if (!iv_tracking_status.isEnabled())
                    iv_tracking_status.setEnabled(true);
            } else {
                // diable switch tracking status button
                if (iv_tracking_status.isEnabled()) {

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (iv_tracking_status.isSelected()) {
                                iv_tracking_status.performClick();
                            }

                            iv_tracking_status.setEnabled(false);
                        }
                    });
                }
            }

            byte[] gimbalMode = recvMessage.getGimbalMode();

            if (Arrays.equals(gimbalMode, GimbalMobileBLEProtocol.GIMBALMODE_FACEFOLLOW)) {
                // can tracking
                canTracking = true;
                cvCamera.startTracking = true;
            } else {
                cvCamera.startTracking = false;
                if (iv_tracking_status.isSelected()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_tracking_status.performClick();
                        }
                    });
                }
            }
        }
    }

    private class BLEConnectingListener implements BLEDriven.ConnectingStatusCallback {

        @Override
        public void onConnecting(int status) {
            switch (status) {
                case BLEDriven.CONNECTED:
                    Log.d("onConnecting", "CONNECTED");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar_connecting_ble_device.setVisibility(View.GONE);
                            iv_tracking_status.setEnabled(true);
                        }
                    });

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (bluetooth_devices_list_view.getVisibility() != View.GONE) {
                                bluetooth_devices_list_view.setVisibility(View.GONE);
                            }
                        }
                    }, 1000);
                    datasourceChanged(bluetoothDeviceList, bluetoothDevice);
                    break;
                case BLEDriven.CONNECTING:
                    Log.d("onConnecting", "CONNECTING");
                    iv_tracking_status.setEnabled(false);
                    break;
                case BLEDriven.DISCONNECTED:
                    iv_tracking_status.setEnabled(false);
                    bluetoothDeviceList.clear();
                    bluetoothDevice = null;
                    datasourceChanged(bluetoothDeviceList, null);
                    Log.d("onConnecting", "DISCONNECTED");

                    break;
            }
        }
    }

    private class BLEDeviceListUpdateListener implements BLEDriven.BLEDeviceListUpdateCallback {

        @Override
        public void onBLEDeviceListUpdate(List<BluetoothDevice> bluetoothDeviceList,
                                          BluetoothDevice connectedDevice) {
            MainActivity.this.bluetoothDeviceList = bluetoothDeviceList;
            datasourceChanged(bluetoothDeviceList, connectedDevice);
        }
    }

    private void datasourceChanged(List<BluetoothDevice> bluetoothDeviceList, BluetoothDevice connectedDevice) {
        bluetoothDevicesListDataSource.setBluetoothDevicesListData(bluetoothDeviceList, connectedDevice);
        bluetoothDevicesListAdapter.setDataSource(bluetoothDevicesListDataSource.getBluetoothDevicesListData());
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bluetoothDevicesListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        checkPermission();

        initView();
        initCvCamera();
        initBLEDriven();
        initTracking();

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("startLive");
//                RTMPClient.instance().connect("rtmp://192.168.1.77:1935/gzhm/room", new RTMPClient.ConnectStateCallback() {
//                    @Override
//                    public void connectState(boolean state) {
//                        if (state) {
//                            System.out.println("connect success");
//                            cvCamera.startLive = true;
//                        } else {
//                            System.out.println("connect failed");
//                        }
//                    }
//                });
//
//            }
//        }, 5000);
    }

    private void initBLEDriven() {
        bleDriven = new BLEDriven(MainActivity.this);
        bleDriven.setBleDeviceListUpdateCallback(new BLEDeviceListUpdateListener());
        bleDriven.setRecvCallback(new RecvDataListener());
        bleDriven.setConnectingStatusCallback(new BLEConnectingListener());
    }

    private void initTracking() {
        cmtTracker = new CMTTracker();

        trackingThread = new HandlerThread("trackingThread");
        trackingThread.start();
        trackingThreadHandler = new Handler(trackingThread.getLooper());
    }

    private void initCvCamera() {
        cvCamera = new CVCamera();
        cvCamera.delegate = MainActivity.this;
//        cvCamera.frameListener = this;
        cameraEngine = cvCamera.cameraEngine;
        cameraEngine.context = MainActivity.this;
        cameraEngine.setDetectFaceListener(this);
    }

    private MotionOrientation motionOrientation;

    @Override
    protected void onResume() {
        super.onResume();

        if ((cameraView != null) && (cameraEngine != null)) {
            SurfaceTexture cameraTexture = cameraView.getCameraTexture();
            if (cameraTexture != null) {
                cameraEngine.openCamera();
                cameraEngine.startPreview(cameraTexture);
            } else {
                cameraView.setSurfaceTextureCallback(new GLTextureView.SurfaceTextureListener() {

                    @Override
                    public void onTextureAvailable(@NotNull final SurfaceTexture texture) {
                        Log.d("SurfaceTextureListener", "onSurfaceTextureAvailable");
//                        MainActivity.this.surfaceTexture = texture;
                        if (allPermissionGranted) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    cameraEngine.openCamera(CameraEngine.CAMERA_FRONT);
                                    cameraEngine.startPreview(texture);
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onSurfaceTextureUpdated(@org.jetbrains.annotations.Nullable SurfaceTexture surface) {

                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(@org.jetbrains.annotations.Nullable SurfaceTexture surface, int width, int height) {

                    }

                    @Override
                    public void onSurfaceTextureAvailable(@org.jetbrains.annotations.Nullable SurfaceTexture surface, int width, int height) {

                    }

                    @Override
                    public boolean onSurfaceTextureDestroyed(@org.jetbrains.annotations.Nullable SurfaceTexture surface) {
                        if (cameraEngine != null)
                            cameraEngine.releaseCamera();

                        return true;
                    }
                });
            }
        }

        motionOrientation = MotionOrientation.init(this);
        if (motionOrientation != null) {
            motionOrientation.setDeviceOrientationListener(this);
        }

        originX = leftbar.getX();
        originY = iv_ble.getY();
        originY2 = iv_switch_camera_mode.getY();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (cameraView != null) {
            if (cameraView.getRecording()) {
                cameraView.stopRecord(new GLTextureView.RecordStatusCallback() {
                    @Override
                    public void recordFinish(@org.jetbrains.annotations.Nullable String moviePath) {
                        Log.d("recordFinish", moviePath);
                        MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.parse("file://" + moviePath)));
                    }
                }, null);
            }
        }

        if (cameraEngine != null) {
            cameraEngine.releaseCamera();
        }

        motionOrientation.releaseSensor();
    }

    private Long pressBackTimePre = 0L;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Long now = System.currentTimeMillis();
            if ((now - pressBackTimePre) > 1000) {
                pressBackTimePre = now;
                Toast.makeText(getApplicationContext(), "double click back to exit", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Toast.makeText(getApplicationContext(), "app exit", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.finish();
                        System.exit(0);
                    }
                }, 1000);
                return super.onKeyDown(keyCode, event);
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        android.graphics.Point screenSize = new android.graphics.Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        SCREEN_WIDTH = screenSize.x;
        SCREEN_HEIGHT = screenSize.y;
        SCALE = SCREEN_WIDTH / 128;

        iv_tracking_status.setEnabled(false);

        trackingBoxUtils = new ViewUtils(trackingBox);

        initBluetoothDevicesList();
    }

    private void initBluetoothDevicesList() {
        bluetooth_devices_list.setLayoutManager(new LinearLayoutManager(this));

        bluetoothDevicesListAdapter = new BluetoothDevicesListAdapter(this);
        bluetoothDevicesListAdapter.setDataSource(
                bluetoothDevicesListDataSource.getBluetoothDevicesListData());
        bluetoothDevicesListAdapter.setCellClickCallback(cellOnClickListener);
        bluetooth_devices_list.setAdapter(bluetoothDevicesListAdapter);
    }

    @Override
    public void processingFrame(Mat mat) {
        cvCamera.startTracking = false;
//        Log.d("processingFrame", mat.size().toString());
        if (trackingThreadHandler != null) {
            trackingThreadHandler.post(new TrackingRunnable(mat));
        } else {
            initTracking();
        }
    }


    @Override
    public void onFrame(ByteBuffer frameBuffer, int frameWidth, int frameHeight) {
        cvCamera.startTracking = false;

        if (trackingThreadHandler != null) {
            trackingThreadHandler.post(new TrackingRunnable(frameBuffer, frameWidth, frameHeight));
        } else {
            initTracking();
        }
    }

    private class TrackingRunnable implements Runnable {
        private Mat mat;

        private ByteBuffer frameBuffer;
        private int frameWidth;
        private int frameHeight;

        TrackingRunnable(Mat mat) {
            this.mat = mat;
        }

        TrackingRunnable(ByteBuffer frameBuffer, int frameWidth, int frameHeight) {
            this.frameBuffer = frameBuffer;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
        }

        @Override
        public void run() {
            if (canTrackerInit) {
                int x = (int) startPoint.x;
                int y = (int) startPoint.y;
                int w = (int) (startPoint.x - endPoint.x);
                int h = (int) (startPoint.y - endPoint.y);

                if (w < 0) {
                    w = -w;
                } else {
                    x = (int) endPoint.x;
                }

                if (h < 0) {
                    h = -h;
                } else {
                    y = (int) endPoint.y;
                }
                x /= SCALE;
                y /= SCALE;
                w /= SCALE;
                h /= SCALE;
                cmtTracker.OpenCMT(mat.getNativeObjAddr(), x, y, w, h);
//                cmtTracker.CMTInit(frameBuffer, frameWidth, frameHeight, x, y, w, h, cameraEngine.isFrontCamera());
                canTrackerInit = false;
                startTracking = true;
            }

            if (startTracking) {
                final int[] rect = new int[4];
                 if (cmtTracker.ProcessCMT(mat.getNativeObjAddr(), rect)) {
//                if (cmtTracker.CMTProcessing(frameBuffer, frameWidth, frameHeight, rect, cameraEngine.isFrontCamera())) {
                    cvCamera.startTracking = true;

                    sendMessage.setTrackingQuailty(GimbalMobileBLEProtocol.TRACKING_QUALITY_GOOD);

                    final Point p = new Point(rect[0], rect[1]);

                    final int width = rect[2];
                    final int height = rect[3];

//                    Log.d("CMTgetRect", p.toString() + " [" + width + "," + height + "]");

//                    cameraEngine.setMeteringAreas((int)p.x*SCALE, (int)p.y*SCALE, width*SCALE, height*SCALE);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final int w = ((rect[2]*SCALE)>SCREEN_WIDTH) ? SCREEN_WIDTH : rect[2]*SCALE;
                            final int h = (((rect[3]*SCALE)>SCREEN_HEIGHT) ? SCREEN_HEIGHT : rect[3]*SCALE);

                            if (w < boxWidth && h < boxHeight) {
                                trackingBoxUtils.setRect((int) p.x * SCALE, (int) p.y * SCALE, w, h, 0);
                            } else {
                                trackingBoxUtils.setRect((int) p.x * SCALE, (int) p.y * SCALE, (int)boxWidth, (int)boxHeight, 0);
                            }

                            trackingBox.setVisibility(View.VISIBLE);
                        }
                    });

                    {
                        int xoffset;
                        int yoffset;

                        if (cameraEngine.isFrontCamera()) {
                            xoffset = (int) (128/2 - (p.x + width/2.0));
                            yoffset = (int) (72/2 - (p.y + height/2.0));
                        } else {
                            xoffset = (int) (-128/2 + (p.x + width/2.0));
                            yoffset = (int) (-72/2 + (p.y + height/2.0));
                        }

                        xoffset *= 10;
                        yoffset *= 10;

//                        Log.d("tracking...", "[" + xoffset + "," + yoffset + "]");

                        int deviceOrientation = MotionOrientation.getDEVICE_ORIENTATION();
                        if (deviceOrientation == MotionOrientation.getDEVICE_ORIENTATION_PORTRAIT()) {
                            int xoffset_t = xoffset;
                            xoffset = -yoffset;
                            yoffset = xoffset_t;
                        } else if (deviceOrientation == MotionOrientation.getDEVICE_ORIENTATION_UPSIDEDOWN()) {
                            int xoffset_t = xoffset;
                            xoffset = yoffset;
                            yoffset = -xoffset_t;
                        } else if (deviceOrientation == MotionOrientation.getDEVICE_ORIENTATION_LANDSCAPERIGHT()) {

                        } else if (deviceOrientation == MotionOrientation.getDEVICE_ORIENTATION_LANDSCAPELEFT()) {
                            xoffset = -xoffset;
                            yoffset = -yoffset;
                        }

                        sendMessage.setXoffset(TypeConversion.intToBytes(xoffset));
                        sendMessage.setYoffset(TypeConversion.intToBytes(yoffset));
                    }

                } else {
                    cvCamera.startTracking = true;
                    sendMessage.setTrackingQuailty(GimbalMobileBLEProtocol.TRACKING_QUALITY_WEAK);
                    sendMessage.setXoffset(TypeConversion.intToBytes(0));
                    sendMessage.setYoffset(TypeConversion.intToBytes(0));
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            trackingBox.setVisibility(View.GONE);
                        }
                    });
                }

            } else {
                if (trackingBox.getVisibility() == View.VISIBLE) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            trackingBox.setVisibility(View.GONE);
                        }
                    });
                }
            }

/*            final Bitmap img = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, img);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    testImageView.setVisibility(View.VISIBLE);
                    testImageView.setImageBitmap(img);
                }
            });*/
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BLEDriven.BLUETOOTH_REQUEST_ON:
                bleDriven.onActivityResultCallback(requestCode, resultCode, data);
                break;
            case REQUEST_BLUETOOTH_ON_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    bleDriven.onActivityResultCallback(requestCode, resultCode, data);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            iv_ble.performClick();
                        }
                    }, 500);
                }
                break;
        }
    }
}
