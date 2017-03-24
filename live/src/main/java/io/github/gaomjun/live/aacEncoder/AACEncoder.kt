package io.github.gaomjun.live.aacEncoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import io.github.gaomjun.libyuv.YUVNative
import io.github.gaomjun.live.encodeConfiguration.AudioConfiguration
import io.github.gaomjun.live.rtmpClient.RTMPClient
import io.github.gaomjun.live.rtmpFrame.RTMPAudioFrame
import java.io.*

/**
 * Created by qq on 25/1/2017.
 */
class AACEncoder(configuration: AudioConfiguration) {
    private var codec: MediaCodec? = null

    private val bitrate: Int
    private val channels: Int
    private val samplerate: Int

    private val aacEncodingThread = HandlerThread("aacEncodingThread")
    private var aacEncodingHandler: Handler? = null

    private var bufferedOutputStream: BufferedOutputStream? = null

    private val rtmpClient = RTMPClient.instance()
    private val audioFrame = RTMPAudioFrame.instance()

    private var saveToFile = false

    init {
        bitrate = configuration.bitrate
        channels = configuration.channels
        samplerate = configuration.samplerate

        initEncoder()

        aacEncodingThread.start()
        aacEncodingHandler = Handler(aacEncodingThread.looper)
    }

    private fun initEncoder() {

        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, samplerate, channels)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, samplerate)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)

        codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        codec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        codec?.start()
    }

    fun encoding(data: ByteArray, timestamp: Long) {
        aacEncodingHandler?.post(AACEncodingRunnable(data, timestamp))
    }

    fun stop() {
        if (saveToFile) {
            saveToFile = false

            bufferedOutputStream?.flush()
            bufferedOutputStream?.close()

            println("aac saveToFile")
        }
    }

    inner class AACEncodingRunnable(val data: ByteArray, val timestamp: Long) : Runnable {
        override fun run() {

            val inputBufferIndex = codec?.dequeueInputBuffer(1000)

            if (inputBufferIndex!! >= 0) {
                val inputBuffer = codec?.getInputBuffer(inputBufferIndex)
                inputBuffer!!.put(data)
                codec?.queueInputBuffer(inputBufferIndex, 0, data.size, 0, 0)
            }

            val bufferInfo = MediaCodec.BufferInfo()
            val outputBufferIndex = codec?.dequeueOutputBuffer(bufferInfo, 0)
            if (outputBufferIndex!! >= 0) {
                val outputBuffer = codec?.getOutputBuffer(outputBufferIndex)

                val aacData = ByteArray(bufferInfo.size)
                outputBuffer!!.get(aacData)

//                MediaFormat outputFormat = codec.getOutputFormat(outputBufferIndex);

                audioFrame.timestamp = timestamp
                audioFrame.data = aacData

                rtmpClient.sendFrame(audioFrame)

                if (saveToFile) {
                    Thread(Runnable {
                        if (bufferedOutputStream == null) {
                            val f = File(Environment.getExternalStorageDirectory(), "Download/audio_encoded.aac")
                            if (f.exists()) {
                                f.delete()
                                println("rm " + f.absolutePath)
                            }
                            bufferedOutputStream = BufferedOutputStream(FileOutputStream(f))

                            Handler(Looper.getMainLooper()).postDelayed({
                                stop()
                            }, (10 * 1000).toLong())
                        }

                        bufferedOutputStream?.write(addADTStoPacket(aacData.size))
                        println("write adts")
                        bufferedOutputStream?.write(aacData)
                        println("write aac")

                    }).start()
                }

//                println("AACEncodingRunnable out data " + bufferInfo.size)

                codec?.releaseOutputBuffer(outputBufferIndex, false)
            }
        }

        private fun addADTStoPacket(dataLength: Int): ByteArray {
            val packetLen = dataLength + 7

            val packet = ByteArray(7)

            val profile = 2  //AAC LC
            //39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
            val freqIdx = 44100  //44.1KHz
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


