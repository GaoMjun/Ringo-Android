package io.github.gaomjun.ringo.BluetoothDevicesList.DataSource;

import android.bluetooth.BluetoothDevice;

/**
 * Created by qq on 28/11/2016.
 */

public class BluetoothDevicesListCell {
    private BluetoothDevice bluetoothDevice;
    private String bluetoothDeviceName;
    private boolean isConnected;

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getBluetoothDeviceName() {
        return bluetoothDeviceName;
    }

    public void setBluetoothDeviceName(String bluetoothDeviceName) {
        this.bluetoothDeviceName = bluetoothDeviceName;
    }
}
