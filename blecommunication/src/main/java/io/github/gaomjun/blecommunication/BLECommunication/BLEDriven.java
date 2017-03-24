package io.github.gaomjun.blecommunication.BLECommunication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.gaomjun.blecommunication.BLECommunication.Message.RecvMessage;
import io.github.gaomjun.blecommunication.BLECommunication.Message.SendMessage;

import static android.bluetooth.BluetoothProfile.GATT;

/**
 * Created by qq on 28/11/2016.
 */

public class BLEDriven {
    public static final int BLUETOOTH_REQUEST_ON = 10;
    private SendMessage sendMessage = SendMessage.getInstance();

    private BluetoothManager bluetoothManager;

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private BluetoothGattService bluetoothGattService;
    private BluetoothDevice bluetoothDevice;

    public boolean connectedToDevice = false;

    private static final String SERVICE_UUID = "0000ff00-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_UUID = "0000ff01-0000-1000-8000-00805f9b34fb";
    private static final String DISCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();

    private final Context context;
    private BluetoothAdapter bluetoothManagerAdapter = null;
    private BluetoothLeScanner bluetoothLeScanner = null;
    private android.bluetooth.BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d("onConnectionStateChange", gatt.getDevice().getName());

                    bluetoothGatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    connectingStatusCallback.onConnecting(DISCONNECTED);
                    connectedToDevice = false;
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID));
                if (service != null) {
                    bluetoothGattService = service;
                    BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(
                            UUID.fromString(CHARACTERISTIC_UUID));
                    if (characteristic != null) {
                        bluetoothGattCharacteristic = characteristic;

                        bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                        BluetoothGattDescriptor descriptor =
                                bluetoothGattCharacteristic.getDescriptor(UUID.fromString(DISCRIPTOR_UUID));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            bluetoothGatt.writeDescriptor(descriptor);
                        }

//                        new WriteThread().start();
                        connectingStatusCallback.onConnecting(CONNECTED);
                        connectedToDevice = true;
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            byte[] data = characteristic.getValue();
            if (data.length > 0) {
                synchronized (BLEDriven.class) {
                    RecvMessage.getInstance().setMessage(data);
                    if (RecvMessage.getInstance().checkMessage())
                        recvCallback.onRecvData(RecvMessage.getInstance());
                }
            }
        }
    };

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            ScanRecord scanRecord = result.getScanRecord();

            String name = device.getName();
            if (name == null) return;
            if (name.length() > 0 && rssi > -80) {
                if (!bluetoothDeviceList.contains(device)) {
                    bluetoothDeviceList.add(device);

                    bleDeviceListUpdateCallback.onBLEDeviceListUpdate(bluetoothDeviceList, bluetoothDevice);
                }

                Log.d("onScanResult", name);
            } else {
                Log.d("onScanResult", name + " " + rssi + " weak signal");
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d("onBatchScanResults", "");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("onScanFailed", errorCode + "");
        }
    };

    public BLEDriven(Context context) {
        this.context = context;

        setUpBLE();

        writeHandlerThread.start();
        writeHandler = new Handler(writeHandlerThread.getLooper());
    }

    private void setUpBLE() {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothManagerAdapter = bluetoothManager.getAdapter();

        if (bluetoothManagerAdapter == null || !bluetoothManagerAdapter.isEnabled()) {
            //TODO
            ((Activity) context).startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_REQUEST_ON);
            return;
        }

        bluetoothLeScanner = bluetoothManagerAdapter.getBluetoothLeScanner();

        new WriteThread().start();

        bluetoothIsAvailable = true;
    }

    public void scanDevices() {
        System.out.println("scanDevices");

        bluetoothDeviceList.clear();
        if (bluetoothDevice != null) {
            bluetoothDeviceList.add(bluetoothDevice);
        }
        bluetoothLeScanner.startScan(getScanFilters(), getScanSettings(), scanCallback);
    }

    private ScanSettings getScanSettings() {
        return new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    }

    private List<ScanFilter> getScanFilters() {
        UUID serviceUUID = UUID.fromString(SERVICE_UUID);
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setServiceUuid(ParcelUuid.fromString(serviceUUID.toString()));
        ScanFilter filters = builder.build();

        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(filters);

        return scanFilters;
    }

    public void stopScanDevices() {
        bluetoothLeScanner.stopScan(scanCallback);
    }

    public void connectToDevice(String address) {
        stopScanDevices();
        if (bluetoothDevice != null) {
            if (bluetoothDevice.getAddress().equals(address)) {
                return;
            }
        }
        for (BluetoothDevice device:
                bluetoothDeviceList) {
            if (device.getAddress().equals(address)) {
                bluetoothDevice = bluetoothManagerAdapter.getRemoteDevice(device.getAddress());
                bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback);
                connectingStatusCallback.onConnecting(CONNECTING);
                connectedToDevice = false;
            }
        }
    }

    public void disconnectDevice() {
        if (bluetoothDevice != null) {
            if (bluetoothManager.getConnectionState(bluetoothDevice, GATT) ==
                    BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt.disconnect();
                bluetoothDevice = null;
                bluetoothGattService = null;
                bluetoothGattCharacteristic = null;
                connectingStatusCallback.onConnecting(DISCONNECTED);
                connectedToDevice = false;
            }
        }
    }

    public void close() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();

            bluetoothGatt = null;
            bluetoothDevice = null;
            bluetoothGattCharacteristic = null;
            bluetoothGattService = null;
            bluetoothManager = null;
            bluetoothManagerAdapter = null;
            bluetoothLeScanner = null;
            bluetoothGattCallback = null;
        }
    }

    public boolean write(byte[] data) {
        bluetoothGattCharacteristic.setValue(data);
        return bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
    }

    public void send(byte[] message) {
        writeHandler.post(new WriteRunnable(message));
    }

    private RecvCallback recvCallback;

    public void setRecvCallback(RecvCallback recvCallback) {
        this.recvCallback = recvCallback;
    }

    public interface RecvCallback {
        void onRecvData(RecvMessage recvMessage);
    }

    private HandlerThread writeHandlerThread = new HandlerThread("writeHandlerThread");
    private Handler writeHandler;

    private class WriteRunnable implements Runnable {

        private byte[] data;

        public WriteRunnable(byte[] data) {
            this.data = data;
        }

        @Override
        public void run() {
            write(data);
        }
    }

    private ConnectingStatusCallback connectingStatusCallback;

    public void setConnectingStatusCallback(ConnectingStatusCallback connectingStatusCallback) {
        this.connectingStatusCallback = connectingStatusCallback;
    }

    public interface ConnectingStatusCallback {
        void onConnecting(int status);
    }

    public final static int CONNECTED = 0;
    public final static int CONNECTING = 1;
    public final static int DISCONNECTED = 2;

    private BLEDeviceListUpdateCallback bleDeviceListUpdateCallback;

    public void setBleDeviceListUpdateCallback(BLEDeviceListUpdateCallback bleDeviceListUpdateCallback) {
        this.bleDeviceListUpdateCallback = bleDeviceListUpdateCallback;
    }

    public interface BLEDeviceListUpdateCallback {
        void onBLEDeviceListUpdate(List<BluetoothDevice> bluetoothDeviceList, BluetoothDevice connectedDevice);
    }

    class WriteThread extends Thread {

        @Override
        public void run() {
            super.run();

            while (!isInterrupted()) {
                if (connectedToDevice) {
                    byte[] message = sendMessage.getMessage();
                    write(message);
//                    Log.d("send", sendMessage.getMessageHexString());
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onActivityResultCallback(int requestCode, int resultCode, Intent data) {
        if ((requestCode == BLUETOOTH_REQUEST_ON) ||
                (requestCode == 11)) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    bluetoothLeScanner = bluetoothManagerAdapter.getBluetoothLeScanner();

                    new WriteThread().start();
                    bluetoothIsAvailable = true;
                    break;
                case Activity.RESULT_CANCELED:
                    bluetoothIsAvailable = false;
                    break;
            }
        }
    }

    public boolean bluetoothIsAvailable = false;
}
