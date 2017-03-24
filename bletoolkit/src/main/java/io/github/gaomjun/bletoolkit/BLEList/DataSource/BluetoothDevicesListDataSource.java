package io.github.gaomjun.bletoolkit.BLEList.DataSource;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qq on 28/11/2016.
 */

public class BluetoothDevicesListDataSource {
    private List<BluetoothDevice> bluetoothDevicesListData;
    private BluetoothDevice connectedDevice;

    public ArrayList<BluetoothDevicesListCell> getBluetoothDevicesListData() {
        ArrayList<BluetoothDevicesListCell> bluetoothDevicesList = new ArrayList<>();

        if ((bluetoothDevicesListData == null) || (bluetoothDevicesListData.size() == 0)) {
            if (connectedDevice == null) {
                return new ArrayList<BluetoothDevicesListCell>();
            } else {
                BluetoothDevicesListCell cell = new BluetoothDevicesListCell();
                cell.setBluetoothDeviceName(connectedDevice.getName());
                cell.setConnected(true);
                bluetoothDevicesList.add(cell);
            }
        }

        for (BluetoothDevice device:
                bluetoothDevicesListData) {
            BluetoothDevicesListCell cell = new BluetoothDevicesListCell();

            cell.setBluetoothDeviceName(device.getName());
            if (connectedDevice != null) {
                if (connectedDevice.getAddress().equals(device.getAddress())) {
                    cell.setConnected(true);
                } else {
                    cell.setConnected(false);
                }
            } else {
                cell.setConnected(false);
            }

            bluetoothDevicesList.add(cell);
        }

        return bluetoothDevicesList;
    }

    public void setBluetoothDevicesListData(List<BluetoothDevice> bluetoothDevicesListData,
                                            BluetoothDevice connectedDevice) {
        this.bluetoothDevicesListData = bluetoothDevicesListData;
        this.connectedDevice = connectedDevice;
    }
}
