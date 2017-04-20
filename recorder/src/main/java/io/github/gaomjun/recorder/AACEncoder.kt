package io.github.gaomjun.recorder

import android.media.AudioTimestamp
import android.media.MediaCodec
import android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * Created by qq on 15/3/2017.
 */
class AACEncoder : PCMCapture.PCMDataCallback {
    private var codec: MediaCodec? = null
    private var aacEncodingThread: HandlerThread? = null
    private var aacEncodingHandler: Handler? = null

    private var pcmCapture: PCMCapture? = null

    var audioFormatChanged: ((format: MediaFormat) -> Unit)? = null
    var audioFormat: MediaFormat? = null

    var saveAACToFile = false
    private var bufferedOutputStream: BufferedOutputStream? = null

    private var SAMPLE_RATE = 44100
    private var CHANNELS = 2
    private var BITRATE = 192000
    private var MAX_INPUT_SIZE = 14208

    constructor() {
        pcmCapture = PCMCapture()
    }

    constructor(audioConfiguration: AudioConfiguration) {
        pcmCapture = PCMCapture(audioConfiguration)

        SAMPLE_RATE = audioConfiguration.SAMPLE_RATE
        CHANNELS = audioConfiguration.CHANNEL_NUM
        BITRATE = audioConfiguration.BITRATE
        MAX_INPUT_SIZE = audioConfiguration.MAX_INPUT_SIZE
    }

    init {
        initEncoder()
    }

    private fun initEncoder() {
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, SAMPLE_RATE, CHANNELS)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE)
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNELS)
        format.setInteger(MediaFormat.KEY_BIT_RATE, BITRATE)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INPUT_SIZE)

        codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        codec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        codec?.start()
    }

    private fun releaseEncoder() {
        codec?.stop()
        codec?.release()
    }

    fun start(formatChanged: ((format: MediaFormat) -> Unit)? = null) {
        ptsStart = -1
        audioFormatChanged = formatChanged

        aacEncodingThread = HandlerThread("aacEncodingThread")
        aacEncodingThread?.start()
        aacEncodingHandler = Handler(aacEncodingThread?.looper)

        pcmCapture?.savePCMToFile = saveAACToFile
        pcmCapture?.pcmDataCallback = this
        pcmCapture?.start()
    }

    fun stop() {
        pcmCapture?.pcmDataCallback = null
        pcmCapture?.stop()

        if (aacEncodingThread != null) {
            val moribund = aacEncodingThread
            aacEncodingThread = null
            moribund!!.interrupt()
        }
    }

    interface AudioDataListener {
        fun onPCMData(data: ByteArray, size: Int, timestamp: Long) {}
        fun onAACData(byteBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {}
    }

    var audioDataListener: AudioDataListener? = null

    private var ptsStart: Long = -1
    private inner class AACEncodingRunnable(val data: ByteArray, val size: Int, val timestamp: Long) : Runnable {
        override fun run() {

            val inputBufferIndex = codec?.dequeueInputBuffer(10000)

            if (inputBufferIndex!! >= 0) {
                val inputBuffer = codec?.getInputBuffer(inputBufferIndex)
                inputBuffer?.position(0)
                inputBuffer?.put(data, 0, size)

                codec?.queueInputBuffer(inputBufferIndex, 0, data.size, 0, 0)
            }

            val bufferInfo = MediaCodec.BufferInfo()
            val outputBufferIndex = codec?.dequeueOutputBuffer(bufferInfo, 0)
            if (outputBufferIndex == INFO_OUTPUT_FORMAT_CHANGED) {
                audioFormat = codec?.outputFormat
                audioFormatChanged?.invoke(audioFormat!!)
            }
            if (outputBufferIndex!! >= 0) {
                bufferInfo.presentationTimeUs = timestamp
                val aacDataBuffer = codec?.getOutputBuffer(outputBufferIndex)!!
                audioDataListener?.onAACData(aacDataBuffer, bufferInfo)

                if (saveAACToFile) {
                    if (bufferedOutputStream == null) {
                        val f = File(Environment.getExternalStorageDirectory(), "DCIM/Camera/audio.aac")
                        if (f.exists()) {
                            f.delete()
                            println("rm " + f.absolutePath)
                        }
                        bufferedOutputStream = BufferedOutputStream(FileOutputStream(f))
                    }

                    val aacData = ByteArray(bufferInfo.size)
                    aacDataBuffer.get(aacData)
                    bufferedOutputStream?.write(addADTStoPacket(aacData.size))
                    bufferedOutputStream?.write(aacData)
                }

                codec?.releaseOutputBuffer(outputBufferIndex, false)
            }
        }
    }

    override fun onPCMData(data: ByteArray, size: Int, timestamp: Long) {
//        println("onPCMData $size")

        aacEncodingHandler?.post(AACEncodingRunnable(data, size, timestamp))
        audioDataListener?.onPCMData(data, size, timestamp)
    }

    companion object {
        // https://wiki.multimedia.cx/index.php/ADTS
        private fun addADTStoPacket(dataLength: Int): ByteArray {
            val packetLen = dataLength + 7

            val packet = ByteArray(7)

            val profile = 2  //AAC LC
            //39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
            val freqIdx = 4  //44.1KHz
            val chanCfg = 2  //CPE

            // fill in ADTS data
            packet[0] = 0xFF.toByte()
            packet[1] = 0xF9.toByte()
            packet[2] = (((profile - 1) shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
            packet[3] = (((chanCfg and 3) shl 6) + (packetLen shr 11)).toByte()
            packet[4] = ((packetLen and 0x7FF) shr 3).toByte()
            packet[5] = (((packetLen and 7) shl 5) + 0x1F).toByte()
            packet[6] = 0xFC.toByte()

            return packet
        }
    }
}