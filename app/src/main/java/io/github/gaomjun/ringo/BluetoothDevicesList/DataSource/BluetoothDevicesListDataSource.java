package io.github.gaomjun.ringo.BluetoothDevicesList.DataSource;

import java.util.ArrayList;

/**
 * Created by qq on 28/11/2016.
 */

public class BluetoothDevicesListDataSource {
    private ArrayList<BluetoothDevicesListCell> bluetoothDevicesListData;

    public ArrayList<BluetoothDevicesListCell> getBluetoothDevicesListData() {
        if (bluetoothDevicesListData != null)
            return bluetoothDevicesListData;

        bluetoothDevicesListData = new ArrayList<>();

        final String[] deviceNameArray = {"aa", "bb", "cc"};
        for (int i = 0; i < 5; i++) {

            for (String deviceName:
                    deviceNameArray) {
                BluetoothDevicesListCell cell = new BluetoothDevicesListCell();
                cell.setBluetoothDeviceName(deviceName);
                cell.setConnected(false);

                bluetoothDevicesListData.add(cell);
            }
        }
        return bluetoothDevicesListData;
    }

    public void setBluetoothDevicesListData(ArrayList<BluetoothDevicesListCell> bluetoothDevicesListData) {
        this.bluetoothDevicesListData = bluetoothDevicesListData;
    }
}
