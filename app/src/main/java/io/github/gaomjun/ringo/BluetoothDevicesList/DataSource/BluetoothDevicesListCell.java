package io.github.gaomjun.ringo.BluetoothDevicesList.DataSource;

/**
 * Created by qq on 28/11/2016.
 */

public class BluetoothDevicesListCell {
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
