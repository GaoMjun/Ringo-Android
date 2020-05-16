package io.github.gaomjun.bletoolkit

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.gaomjun.blecommunication.BLECommunication.BLEDriven
import io.github.gaomjun.blecommunication.BLECommunication.Message.RecvMessage
import io.github.gaomjun.bletoolkit.BLEList.Adapter.BluetoothDevicesListAdapter
import io.github.gaomjun.bletoolkit.BLEList.DataSource.BluetoothDevicesListDataSource
import io.github.gaomjun.utils.TypeConversion.HEXString
import java.util.*

class MainActivity : Activity() {

    private var bleDevicesRecyclerView: RecyclerView? = null
    private var bluetoothDevicesListAdapter: BluetoothDevicesListAdapter? = null
    private val bluetoothDevicesListDataSource = BluetoothDevicesListDataSource()
    private var  bluetoothDevice: BluetoothDevice? = null
    private var bluetoothDeviceList: List<BluetoothDevice> = ArrayList()
    var bleDriven: BLEDriven? = null
    private var progressBar_connecting_ble_device: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        initBluetoothDevicesList()

        initBLEDriven()
    }

    override fun onResume() {
        super.onResume()

        bleDriven?.scanDevices()
    }

    override fun onPause() {
        super.onPause()

        bleDriven?.stopScanDevices()

    }

    private fun initBluetoothDevicesList() {
        bleDevicesRecyclerView = findViewById(R.id.bleDevicesRecyclerView) as RecyclerView

        bleDevicesRecyclerView?.layoutManager = LinearLayoutManager(this)

        bluetoothDevicesListAdapter = BluetoothDevicesListAdapter(this)
        bluetoothDevicesListAdapter?.setDataSource(
                bluetoothDevicesListDataSource.bluetoothDevicesListData)
        bluetoothDevicesListAdapter?.setCellClickCallback(cellOnClickListener)
        bleDevicesRecyclerView?.adapter = bluetoothDevicesListAdapter
    }

    private fun initBLEDriven() {
        bleDriven = BLEDriven(this)
        bleDriven?.setBleDeviceListUpdateCallback(BLEDeviceListUpdateListener())
        bleDriven?.setRecvCallback(RecvDataListener())
        bleDriven?.setConnectingStatusCallback(BLEConnectingListener())
    }

    private val cellOnClickListener = BluetoothDevicesListAdapter.CellClickCallback {
        progressBar_connecting_ble_device, position ->

        Log.d("cellOnClick", "" + position)

        if (bluetoothDevice != null) {
            if (bluetoothDevice?.address == bluetoothDeviceList[position].address) {
                return@CellClickCallback
            } else {
                bleDriven?.disconnectDevice()
                bluetoothDevice = null
            }
        }
        bluetoothDevice = bluetoothDeviceList[position]
        Log.d("connectToDevice", bluetoothDevice?.name)

        bleDriven?.connectToDevice(bluetoothDevice?.address)
        this@MainActivity.progressBar_connecting_ble_device = progressBar_connecting_ble_device
        progressBar_connecting_ble_device.visibility = View.VISIBLE
    }

    private inner class BLEDeviceListUpdateListener : BLEDriven.BLEDeviceListUpdateCallback {

        override fun onBLEDeviceListUpdate(bluetoothDeviceList: List<BluetoothDevice>, connectedDevice: BluetoothDevice?) {

            this@MainActivity.bluetoothDeviceList = bluetoothDeviceList
            datasourceChanged(bluetoothDeviceList, connectedDevice)
        }
    }

    private fun datasourceChanged(bluetoothDeviceList: List<BluetoothDevice>, connectedDevice: BluetoothDevice?) {

        bluetoothDevicesListDataSource.setBluetoothDevicesListData(bluetoothDeviceList, connectedDevice)

        bluetoothDevicesListAdapter?.setDataSource(bluetoothDevicesListDataSource.bluetoothDevicesListData)

        this@MainActivity.runOnUiThread {
            bluetoothDevicesListAdapter?.notifyDataSetChanged()
        }
    }

    private inner class RecvDataListener : BLEDriven.RecvCallback {

        override fun onRecvData(recvMessage: RecvMessage) {
            Log.d("recv", recvMessage.messageHexString)


        }
    }

    private inner class BLEConnectingListener : BLEDriven.ConnectingStatusCallback {

        override fun onConnecting(status: Int) {
            when (status) {
                BLEDriven.CONNECTED -> {
                    Log.d("onConnecting", "CONNECTED")
                    this@MainActivity.runOnUiThread {
                        progressBar_connecting_ble_device?.visibility = View.GONE

//                        val intent = Intent(this@MainActivity, ChangeNameActivity::class.java)
//                        startActivityForResult(intent, 0)

                        runOnUiThread {
                            val inputField = android.widget.EditText(this@MainActivity)
                            AlertDialog.Builder(this@MainActivity)
                                    .setTitle("input name")
                                    .setView(inputField)
                                    .setPositiveButton("confirm") {
                                        dialog, which ->
                                        val name = inputField.text.toString()
                                        println(name)
                                        if (name != null) {
                                            changeName(name)
                                        }
                                    }
                                    .show()
                        }
                    }

                    datasourceChanged(bluetoothDeviceList, bluetoothDevice!!)
                }
                BLEDriven.CONNECTING -> {
                    Log.d("onConnecting", "CONNECTING")

                }
                BLEDriven.DISCONNECTED -> {

                    //                    bluetoothDevice = null;
                    //                    datasourceChanged(bluetoothDeviceList, bluetoothDevice);
                    Log.d("onConnecting", "DISCONNECTED")
                }
            }
        }
    }


    private fun changeName(name: String) {
        val nameBytes = name.toByteArray()

        val header = HEXString.hexString2Bytes("FFAA")
        val command = HEXString.hexString2Bytes("07")
        val dataLen = byteArrayOf(nameBytes.size.toByte())

        val checksum = checksum(command, nameBytes)

        val packetLen = header.size + command.size + dataLen.size + nameBytes.size + checksum.size

        val sendBytes = ByteArray(packetLen)
        var index = 0
        System.arraycopy(header, 0, sendBytes, index, header.size)
        index += header.size

        System.arraycopy(command, 0, sendBytes, index, command.size)
        index += command.size

        System.arraycopy(dataLen, 0, sendBytes, index, dataLen.size)
        index += dataLen.size

        System.arraycopy(nameBytes, 0, sendBytes, index, nameBytes.size)
        index += nameBytes.size

        System.arraycopy(checksum, 0, sendBytes, index, checksum.size)

        Thread().run {

            bleDriven?.write(sendBytes)
        }
    }

    private fun checksum(command: ByteArray, nameBytes: ByteArray): ByteArray {
        var checksum = command[0]
        for (byte in nameBytes) {
            checksum = (checksum + byte).toByte()
        }

//        val checksum: Byte = (command[0].toInt() + nameBytes.sumBy { it.toInt() }).toByte()

        return byteArrayOf(checksum)
    }
}
