package io.github.gaomjun.blecommunication.BLECommunication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by qq on 28/11/2016.
 */

public class BLEDriven {

    private final Context context;
    private BluetoothAdapter bluetoothManagerAdapter = null;
    private BluetoothLeScanner bluetoothLeScanner = null;
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
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
    }

    private void setUpBLE() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothManagerAdapter = bluetoothManager.getAdapter();

        if (bluetoothManagerAdapter == null || !bluetoothManagerAdapter.isEnabled()) {
            // displays a dialog requesting user permission to enable Bluetooth.
            Log.w("setUpBLE", "displays a dialog requesting user permission to enable Bluetooth.");
        }

        BluetoothLeScanner bluetoothLeScanner = bluetoothManagerAdapter.getBluetoothLeScanner();
    }

    public void scanDevices() {
        bluetoothLeScanner.startScan(scanCallback);
    }

    public void stopScanDevices() {
        bluetoothLeScanner.stopScan(scanCallback);
    }
}
