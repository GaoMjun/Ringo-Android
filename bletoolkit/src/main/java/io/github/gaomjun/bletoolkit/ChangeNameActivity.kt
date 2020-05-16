package io.github.gaomjun.bletoolkit

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import io.github.gaomjun.utils.TypeConversion.HEXString

/**
 * Created by qq on 16/2/2017.
 */
class ChangeNameActivity : Activity() {

    private var nameEditField: EditText? = null
    private var confirmButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_name)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        bindViews()

        actionHandler()
    }

    private fun actionHandler() {
        confirmButton?.setOnClickListener {
            val name = nameEditField?.text.toString()
            println("input name: $name")

            if (name != null) {
                changeName(name)
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
//            var mainActivity = callingActivity
//            if (mainActivity is MainActivity) {
//
//            }

//            mainActivity.bleDriven?.write(sendBytes)
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

    private fun bindViews() {
        nameEditField = findViewById(R.id.nameEditField) as EditText
        confirmButton = findViewById(R.id.confirmButton) as Button
    }
}