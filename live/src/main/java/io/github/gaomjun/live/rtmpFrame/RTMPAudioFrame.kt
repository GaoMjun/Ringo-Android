package io.github.gaomjun.live.rtmpFrame

import java.util.*

/**
 * Created by qq on 4/2/2017.
 */
class RTMPAudioFrame: RTMPFrame() {
    var info: ByteArray? = null
        get() {
            var asc = 0

            asc = asc or ((2 shl 11) and 0xf800)
            asc = asc or ((4 shl 7) and 0x0780)
            asc = asc or ((2 shl 3) and 0x78)
            asc = asc or ((0 shl 0) and 0x07)

            var buffer = ByteArray(2)

            buffer.set(0, ((asc and 0xff00) shr 8).toByte())
            buffer.set(1, ((asc and 0x00ff) shr 0).toByte())

            return buffer
        }

    private val headherBuffer = ByteArray(4)
    private val bodyBuffer = ByteArray(512)
    private val packetBuffer = ByteArray(512)

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
        val instance = RTMPAudioFrame()
    }

    private fun prepareHeader(): Int {
        var index = 0

        headherBuffer.set(index++, 0xAF.toByte())
        headherBuffer.set(index++, 0x00.toByte())

        System.arraycopy(info, 0, headherBuffer, index, info!!.size)
        index += info!!.size

        return index
    }

    private fun prepareBody(): Int {
        var index = 0

        bodyBuffer.set(index++, 0xAF.toByte())
        bodyBuffer.set(index++, 0x01.toByte())

        System.arraycopy(data, 0, bodyBuffer, index, data!!.size)
        index += data!!.size

        return index
    }
}