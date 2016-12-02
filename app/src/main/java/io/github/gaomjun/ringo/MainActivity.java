package io.github.gaomjun.ringo;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.gaomjun.blecommunication.BLECommunication.BLEDriven;
import io.github.gaomjun.blecommunication.BLECommunication.Message.RecvMessage;
import io.github.gaomjun.blecommunication.BLECommunication.Message.SendMessage;
import io.github.gaomjun.cameraengine.CameraEngine;
import io.github.gaomjun.cmttracker.CMTTracker;
import io.github.gaomjun.cvcamera.CVCamera;
import io.github.gaomjun.ringo.BluetoothDevicesList.Adapter.BluetoothDevicesListAdapter;
import io.github.gaomjun.ringo.BluetoothDevicesList.DataSource.BluetoothDevicesListCell;
import io.github.gaomjun.ringo.BluetoothDevicesList.DataSource.BluetoothDevicesListDataSource;

public class MainActivity extends Activity implements CVCamera.FrameCallback {
    private BLEDriven bleDriven = null;
    private SendMessage sendMessage = SendMessage.getInstance();

    private RecyclerView bluetoothDevicesListRecyclerView;

    private BluetoothDevicesListAdapter bluetoothDevicesListAdapter;
    private BluetoothDevicesListDataSource bluetoothDevicesListDataSource =
            new BluetoothDevicesListDataSource();
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
    private View trackingBox;
    private ImageView testImageView;
    private TextureView cameraView = null;
    private CVCamera cvCamera = null;
    private CameraEngine cameraEngine = null;
    private View.OnClickListener btn_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_capture:
                    cameraEngine.takePicture(new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            cameraEngine.startPreview();
                            if (data != null) {
                                savePhotoToAlbum(data);
                            }
                        }
                    });
                    break;
                case R.id.iv_switch_camera:
                    cameraEngine.switchCamera();
                    break;
                case R.id.bluetooth_devices_list_close:
                    findViewById(R.id.bluetooth_devices_list_view).setVisibility(View.GONE);
                    bleDriven.stopScanDevices();
                    break;
                case R.id.iv_ble:
                    if (findViewById(R.id.bluetooth_devices_list_view).getVisibility() == View.GONE) {
                        findViewById(R.id.bluetooth_devices_list_view).setVisibility(View.VISIBLE);
                        datasourceChanged(new ArrayList<BluetoothDevice>(), bluetoothDevice);
                        bleDriven.scanDevices();
                    } else {
                        findViewById(R.id.bluetooth_devices_list_view).setVisibility(View.GONE);
                        bleDriven.stopScanDevices();
                    }

                    break;
            }
        }
    };
    private BluetoothDevicesListAdapter.CellClickCallback cellOnClickListener =
            new BluetoothDevicesListAdapter.CellClickCallback() {
        @Override
        public void cellOnClick(int position) {
            Log.d("cellOnClick", "" + position);
            Toast.makeText(MainActivity.this, "cellOnClick " + position, Toast.LENGTH_SHORT).show();

            if (bluetoothDevice != null) {
                if (bluetoothDevice.getAddress().equals(bluetoothDeviceList.get(position))) {
                    return;
                } else {
                    bleDriven.disconnectDevice();
                    bluetoothDevice = null;
                }
            }
            bluetoothDevice = bluetoothDeviceList.get(position);
            Log.d("connectToDevice", bluetoothDevice.getName());
            bleDriven.connectToDevice(bluetoothDevice.getAddress());
        }
    };

    private void savePhotoToAlbum(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bitmap != null) {
            File file = new File(Environment.getExternalStorageDirectory() + "/" +
                    Environment.DIRECTORY_DCIM + "/", System.currentTimeMillis() + ".jpg");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

                fileOutputStream.flush();
                fileOutputStream.close();

                Log.d("onPictureTaken", "take picture success");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            final Point point = new Point(event.getX(), event.getY());
            Log.d("OnTouch", point.toString());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startPoint.x = point.x;
                    startPoint.y = point.y;

                    trackingBoxUtils.setX((int) startPoint.x, 0);
                    trackingBoxUtils.setY((int) startPoint.y, 0);

                    Log.d("MotionEvent", "touch start" + startPoint.toString());

                    {
                        canTrackerInit = false;
                        startTracking = false;
                        trackingBox.setVisibility(View.GONE);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    endPoint.x = point.x;
                    endPoint.y = point.y;

                    trackingBox.setVisibility(View.VISIBLE);
                    trackingBoxUtils.setWidth((int) Math.abs(startPoint.x - endPoint.x), 0);
                    trackingBoxUtils.setHeight((int) Math.abs(startPoint.y - endPoint.y), 0);

                    Log.d("MotionEvent", "touch move" + endPoint.toString());
                    break;
                case MotionEvent.ACTION_UP:
                    endPoint.x = point.x;
                    endPoint.y = point.y;

                    if (Math.abs(startPoint.x - endPoint.x) > 100 &&
                        Math.abs(startPoint.y - endPoint.y) > 100) {
                        canTrackerInit = true;
                    } else {
                        canTrackerInit = false;
                    }
                    trackingBoxUtils.setRect(0, 0, 0, 0, 0);
                    trackingBox.setVisibility(View.GONE);
                    Log.d("MotionEvent", "touch end" + endPoint.toString());
                    break;
            }
            return true;
        }
    };
    private boolean canTrackerInit = false;
    private boolean startTracking = false;

    private class RecvDataListener implements BLEDriven.RecvCallback {

        @Override
        public void onRecvData(RecvMessage message) {
            Log.d("recv", message.getMessageHexString());
        }
    }

    private class BLEConnectingListener implements BLEDriven.ConnectingStatusCallback {

        @Override
        public void onConnecting(int status) {
            switch (status) {
                case BLEDriven.CONNECTED:
                    Log.d("onConnecting", "CONNECTED");
                    datasourceChanged(bluetoothDeviceList, bluetoothDevice);
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

        initView();
        initCvCamera();
        initBLEDriven();
        initTracking();
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
        cameraEngine = cvCamera.cameraEngine;
    }

    private void hidenNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!cameraView.isAvailable()) {
            cameraView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    cameraEngine.openCamera();
                    cameraEngine.startPreview(surfaceTexture);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        } else {

        }
    }

    @Override
    protected void onStop() {
        cameraEngine.releaseCamera();

        super.onStop();
    }

    private void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hidenNavigationBar();

        SCREEN_WIDTH = getWindowManager().getDefaultDisplay().getWidth();
        SCREEN_HEIGHT = getWindowManager().getDefaultDisplay().getHeight();
        SCALE = SCREEN_WIDTH / 128;

        cameraView = (TextureView) findViewById(R.id.cameraView);

        findViewById(R.id.iv_capture).setOnClickListener(btn_listener);
        findViewById(R.id.iv_switch_camera).setOnClickListener(btn_listener);
        findViewById(R.id.iv_switch_camera_mode).setOnClickListener(btn_listener);
        findViewById(R.id.iv_tracking_status).setOnClickListener(btn_listener);
        findViewById(R.id.iv_ble).setOnClickListener(btn_listener);
        findViewById(R.id.iv_album).setOnClickListener(btn_listener);
        findViewById(R.id.bluetooth_devices_list_close).setOnClickListener(btn_listener);

        testImageView = (ImageView) findViewById(R.id.testImageView);

        trackingBox = findViewById(R.id.trackingBox);
        trackingBoxUtils = new ViewUtils(trackingBox);

        findViewById(R.id.activity_main).setOnTouchListener(onTouchListener);

        initBluetoothDevicesList();
    }

    private void initBluetoothDevicesList() {
        bluetoothDevicesListRecyclerView = (RecyclerView) findViewById(R.id.bluetooth_devices_list);

        bluetoothDevicesListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        bluetoothDevicesListAdapter = new BluetoothDevicesListAdapter(this);
        bluetoothDevicesListAdapter.setDataSource(bluetoothDevicesListDataSource.getBluetoothDevicesListData());
        bluetoothDevicesListAdapter.setCellClickCallback(cellOnClickListener);
        bluetoothDevicesListRecyclerView.setAdapter(bluetoothDevicesListAdapter);
    }

    @Override
    public void processingFrame(Mat mat) {
        trackingThreadHandler.post(new TrackingRunnable(mat));
    }

    private class TrackingRunnable implements Runnable {
        private Mat mat = null;

        public TrackingRunnable(Mat mat) {
            this.mat = mat;
        }

        @Override
        public void run() {

            Mat smallMat = mat.clone();
            Imgproc.resize(smallMat, smallMat, new org.opencv.core.Size(128, 72));

            if (canTrackerInit) {
                cmtTracker.OpenCMT(smallMat.getNativeObjAddr(),
                        (int) (startPoint.x / SCALE),
                        (int) (startPoint.y / SCALE),
                        (int) (endPoint.x / SCALE),
                        (int) (endPoint.y / SCALE),
                        cameraEngine.isFrontCamera());
                canTrackerInit = false;
                startTracking = true;
            }

            if (startTracking) {
                cmtTracker.ProcessCMT(smallMat.getNativeObjAddr(), cameraEngine.isFrontCamera());
                int[] rect = cmtTracker.CMTgetRect();

                if (cmtTracker.CMTgetResult()) {

                    final Point p = new Point(rect[0], rect[1]);

                    final int width = rect[2];
                    final int height = rect[3];

                    Log.d("CMTgetRect", p.toString() + " [" + width + "," + height + "]");

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            trackingBoxUtils.setRect((int) p.x * SCALE, (int) p.y * SCALE, width * SCALE, height * SCALE, 0);
                            trackingBox.setVisibility(View.VISIBLE);
                        }
                    });

                    {
                        int xoffset = (int) (SCREEN_WIDTH / 2 - p.x);
                        int yoffset = (int) (SCREEN_HEIGHT / 2 - p.y);
                        Log.d("tracking...", "[" + xoffset + "," + yoffset + "]");

                        sendMessage.setXoffset();
                    }

                } else {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            trackingBox.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }
    }
}
