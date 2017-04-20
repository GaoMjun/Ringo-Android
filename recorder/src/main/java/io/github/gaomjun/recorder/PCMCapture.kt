package io.github.gaomjun.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.os.SystemClock
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by qq on 15/3/2017.
 */
class PCMCapture {
    private var audioRecord: AudioRecord? = null

    private var audioRecordThread: AudioRecordThread? = null
    private var bufferSizeInBytes: Int = 0
    private var pcmData: ByteArray? = null

    private @Volatile var exit = false

    var savePCMToFile = false
    private var bufferedOutputStream: BufferedOutputStream? = null

    private var SAMPLE_RATE = 44100
    private var CHANNEL_NUM = 2
    private var BITS_PER_SAMPLE = 16
    private var LENGTH_PER_READ = 4096

    constructor()

    constructor(audioConfiguration: AudioConfiguration) {
        SAMPLE_RATE = audioConfiguration.SAMPLE_RATE
        CHANNEL_NUM = audioConfiguration.CHANNEL_NUM
        BITS_PER_SAMPLE = audioConfiguration.BITS_PER_SAMPLE
        LENGTH_PER_READ = audioConfiguration.LENGTH_PER_READ
    }

    fun start() {
        initAudiorecord()

        audioRecord?.startRecording()

        exit = false
        audioRecordThread = AudioRecordThread()
        audioRecordThread?.start()
    }

    fun stop() {

        exit = true
        audioRecordThread = null

        audioRecord?.stop()
        audioRecord?.release()
    }

    private fun initAudiorecord() {
        val audioSource = MediaRecorder.AudioSource.CAMCORDER
        val sampleRateInHz = SAMPLE_RATE
        val channelConfig = AudioFormat.CHANNEL_IN_STEREO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat) * 2

        audioRecord = AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes)
    }

    private inner class AudioRecordThread : Thread() {
        override fun run() {
            super.run()

            while (!exit) {
                if (pcmData == null) {
                    pcmData = ByteArray(LENGTH_PER_READ)
                }

                var offset = 0
                while (offset < bufferSizeInBytes) {

                    var readlen = bufferSizeInBytes - offset
                    if (readlen > LENGTH_PER_READ) {
                        readlen = LENGTH_PER_READ
                    }

                    val len = audioRecord?.read(pcmData, offset, readlen)
                    offset += len!!

                    if (len > 0) {
//                        println("audioRecord $len")

                        pcmDataCallback?.onPCMData(pcmData!!, len, System.nanoTime())

                        if (savePCMToFile) {
                            if (bufferedOutputStream == null) {
                                val f = File(Environment.getExternalStorageDirectory(), "DCIM/Camera/audio.wav")
                                if (f.exists()) {
                                    f.delete()
                                    println("rm " + f.absolutePath)
                                }
                                bufferedOutputStream = BufferedOutputStream(FileOutputStream(f))

                                bufferedOutputStream?.write(wavHeader())
                                bufferedOutputStream?.write(pcmData)
                            } else {
                                bufferedOutputStream?.write(pcmData)
                            }
                        }
                    }
                }
            }
        }
    }

    // http://soundfile.sapp.org/doc/WaveFormat/
    private fun wavHeader(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(44)

        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        byteBuffer.putInt(0x46464952)           // ChunkID 4byte, 0x52494646 big-endian form
        byteBuffer.putInt(0)                    // ChunkSize 4byte, 36 + SubChunk2Size, or more precisely: 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
        byteBuffer.putInt(0x45564157)           // Format 4byte, 0x57415645 big-endian form
        byteBuffer.putInt(0x20746d66)           // Subchunk1ID 4byte, 0x666d7420 big-endian form
        byteBuffer.putInt(16)                   // Subchunk1Size 4byte, 16 for PCM
        byteBuffer.putShort(1.toShort())        // AudioFormat 2byte, PCM = 1
        byteBuffer.putShort(CHANNEL_NUM.toShort())        // NumChannels 2byte, Mono = 1, Stereo = 2, etc.
        byteBuffer.putInt(SAMPLE_RATE)                // SampleRate 4byte, 8000, 44100, etc.
        byteBuffer.putInt(SAMPLE_RATE * CHANNEL_NUM * BITS_PER_SAMPLE/8)        // ByteRate 4byte, SampleRate * NumChannels * BitsPerSample/8
        byteBuffer.putShort((CHANNEL_NUM * BITS_PER_SAMPLE/8).toShort())  // BlockAlign 2byte, NumChannels * BitsPerSample/8
        byteBuffer.putShort(BITS_PER_SAMPLE.toShort())       // BitsPerSample 2byte, 8 bits = 8, 16 bits = 16, etc.
        byteBuffer.putInt(0x61746164)           // Subchunk2ID 4byte, 0x64617461 big-endian form
        byteBuffer.putInt(0)                    // Subchunk2Size 4byte, NumSamples * NumChannels * BitsPerSample/8

        return byteBuffer.array()
    }

    interface PCMDataCallback {
        fun onPCMData(data: ByteArray, size: Int, timestamp: Long)
    }

    var pcmDataCallback: PCMDataCallback? = null
}