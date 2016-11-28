package io.github.gaomjun.ringo.BluetoothDevicesList.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.gaomjun.ringo.BluetoothDevicesList.DataSource.BluetoothDevicesListCell;
import io.github.gaomjun.ringo.MainActivity;
import io.github.gaomjun.ringo.R;

/**
 * Created by qq on 28/11/2016.
 */

public class BluetoothDevicesListAdapter extends RecyclerView.Adapter
        <BluetoothDevicesListAdapter.BluetoothDevicesListCellHodler> {

    private ArrayList<BluetoothDevicesListCell> bluetoothDevicesListData;
    private LayoutInflater layoutInflater;

    public BluetoothDevicesListAdapter(Context context) {
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setDataSource(ArrayList<BluetoothDevicesListCell> bluetoothDevicesListData) {
        this.bluetoothDevicesListData = bluetoothDevicesListData;
    }

    @Override
    public BluetoothDevicesListCellHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = layoutInflater.inflate(R.layout.bluetooth_devices_list_cell, parent, false);

        return new BluetoothDevicesListCellHodler(inflate);
    }

    @Override
    public void onBindViewHolder(BluetoothDevicesListCellHodler holder, int position) {
        BluetoothDevicesListCell cell = bluetoothDevicesListData.get(position);

        if (cell.isConnected()) {
            holder.deviceConnectedImage.setVisibility(View.VISIBLE);
        } else {
            holder.deviceConnectedImage.setVisibility(View.INVISIBLE);
        }
        holder.deviceName.setText(cell.getBluetoothDeviceName());
    }

    @Override
    public int getItemCount() {
        return bluetoothDevicesListData.size();
    }

    class BluetoothDevicesListCellHodler extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View cell;
        private TextView deviceName;
        private ImageView deviceConnectedImage;

        public BluetoothDevicesListCellHodler(View itemView) {
            super(itemView);

            cell = itemView.findViewById(R.id.bluetooth_devices_list_cell);
            deviceName = (TextView) itemView.findViewById(R.id.bluetooth_device_name);
            deviceConnectedImage = (ImageView) itemView.findViewById(R.id.bluetooth_device_connected_image);

            cell.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            cellClickCallback.cellOnClick(getAdapterPosition());
        }
    }

    private CellClickCallback cellClickCallback;

    public void setCellClickCallback(CellClickCallback cellClickCallback) {
        this.cellClickCallback = cellClickCallback;
    }

    public interface CellClickCallback {
        void cellOnClick(int position);
    }
}
