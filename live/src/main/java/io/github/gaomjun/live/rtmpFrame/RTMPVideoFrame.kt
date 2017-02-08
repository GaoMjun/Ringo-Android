package io.github.gaomjun.live.rtmpFrame

import java.nio.ByteBuffer
import java.util.*

/**
 * Created by qq on 3/2/2017.
 */
class RTMPVideoFrame: RTMPFrame() {
    var sps: ByteArray? = null
    var pps: ByteArray? = null
    var isKeyFrame: Boolean? = null

    private val headherBuffer = ByteArray(1024)
    private var bodyBuffer = ByteArray(4096)
    private var packetBuffer = ByteArray(4096)

    override val packet: ByteArray?
        get() {
            var index = 0

            val headerSize = prepareHeader()
            val bodySize = prepareBody()

            System.arraycopy(headherBuffer, 0, packetBuffer, index, headerSize)
            index += headerSize

            System.arraycopy(bodyBuffer, 0, packetBuffer, index, bodySize)


            return Arrays.copyOf(packetBuffer, index)
        }

    override val header: ByteArray?
        get() {
            val headerSize = prepareHeader()

            return Arrays.copyOf(headherBuffer, headerSize)
        }

    override val body: ByteArray?
        get() {
            val bodySize = prepareBody()

            return Arrays.copyOf(bodyBuffer, bodySize)
        }

    companion object {
        @JvmStatic fun instance() = Holder.instance
    }

    private object Holder {
        val instance = RTMPVideoFrame()
    }

    private fun prepareHeader(): Int {
        var index = 0

        headherBuffer[index++] = 0x17.toByte()
        headherBuffer[index++] = 0x00.toByte()

        headherBuffer[index++] = 0x00.toByte()
        headherBuffer[index++] = 0x00.toByte()
        headherBuffer[index++] = 0x00.toByte()

        headherBuffer[index++] = 0x01.toByte()
        headherBuffer[index++] = sps!![1]
        headherBuffer[index++] = sps!![2]
        headherBuffer[index++] = sps!![3]
        headherBuffer[index++] = 0xff.toByte()

        // sps
        headherBuffer[index++] = 0xe1.toByte()
        headherBuffer[index++] = ((sps!!.size shr 8) and 0xff).toByte()
        headherBuffer[index++] = ((sps!!.size) and 0xff).toByte()
        System.arraycopy(sps, 0, headherBuffer, index, sps!!.size)
        index += sps!!.size

        // pps
        headherBuffer[index++] = 0x01.toByte()
        headherBuffer[index++] = ((pps!!.size shr 8) and 0xff).toByte()
        headherBuffer[index++] = ((pps!!.size) and 0xff).toByte()
        System.arraycopy(pps, 0, headherBuffer, index, pps!!.size)
        index += pps!!.size

        return index
    }

    private fun prepareBody(): Int {
        if (data!!.size > bodyBuffer.size) {
            bodyBuffer = ByteArray(data!!.size*2)
        }

        var index = 0

        if (isKeyFrame!!) {
            bodyBuffer[index++] = 0x17.toByte()
        } else {
            bodyBuffer[index++] = 0x27.toByte()
        }

        bodyBuffer[index++] = 0x01.toByte()
        bodyBuffer[index++] = 0x00.toByte()
        bodyBuffer[index++] = 0x00.toByte()
        bodyBuffer[index++] = 0x00.toByte()

        val dataSize = data!!.size - 4

        bodyBuffer[index++] = ((dataSize shr 24) and 0xff).toByte()
        bodyBuffer[index++] = ((dataSize shr 16) and 0xff).toByte()
        bodyBuffer[index++] = ((dataSize shr 8) and 0xff).toByte()
        bodyBuffer[index++] = ((dataSize shr 0) and 0xff).toByte()

        System.arraycopy(data, 4, bodyBuffer, index, dataSize)
        index += dataSize

        return index
    }


}