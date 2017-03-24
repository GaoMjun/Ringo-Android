package io.github.gaomjun.audioengine

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import io.github.gaomjun.live.aacEncoder.AACEncoder
import io.github.gaomjun.live.encodeConfiguration.AudioConfiguration
import java.nio.ByteOrder.LITTLE_ENDIAN
import android.R.attr.order
import android.os.Environment
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * Created by qq on 25/1/2017.
 */
class AudioEngine {
    private var audioRecord: AudioRecord? = null
    private var audioRecordThread: AudioRecordThread? = AudioRecordThread()
    private var bufferSizeInBytes: Int? = null
    private var audioData: ByteArray? = null

    private val aacEncoder = AACEncoder(AudioConfiguration.instance())

    private var saveToFile = false

    private var bufferedOutputStream: BufferedOutputStream? = null

    init {
        initAudioRecord()
    }

    fun start() {
        audioRecord?.startRecording()

        if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
            audioRecordThread?.start()
        }
    }

    fun stop() {
        if (saveToFile) {
            saveToFile = false

            bufferedOutputStream?.flush()
            bufferedOutputStream?.close()

            val f = File(Environment.getExternalStorageDirectory(), "Download/audio.wav")
            if (f.exists()) {
                val filePath = f.absolutePath
                val fileSize = f.length()
                prepareWAVSize(filePath, fileSize.toInt())

                println("saveToFile")
            }
        }
    }

    private fun initAudioRecord() {
        val audioSource = MediaRecorder.AudioSource.DEFAULT
        val sampleRateInHz = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat) * 1

        audioRecord = AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes!!)
    }

    private inner class AudioRecordThread : Thread() {
        override fun run() {
            super.run()

            while (!isInterrupted) {
                if (audioData == null) {
                    audioData = ByteArray(bufferSizeInBytes!!)
                }

                val len = audioRecord?.read(audioData, 0, bufferSizeInBytes!!)
                if (len!! > 0) {
//                    println("audioRecord $len")

                    aacEncoder.encoding(audioData!!, System.currentTimeMillis())

                    audioDataCallback?.onAudioData(audioData!!, len)

                    if (saveToFile) {
                        if (bufferedOutputStream == null) {
                            val f = File(Environment.getExternalStorageDirectory(), "Download/audio.wav")
                            if (f.exists()) {
                                f.delete()
                                println("rm " + f.absolutePath)
                            }
                            bufferedOutputStream = BufferedOutputStream(FileOutputStream(f))

                            bufferedOutputStream?.write(wavHeader())
                            bufferedOutputStream?.write(audioData)
                        } else {
                            bufferedOutputStream?.write(audioData)
                        }
                    }
                }

                Thread.sleep(10)
            }
        }
    }

    interface AudioDataCallback {
        fun onAudioData(data: ByteArray, size: Int)
    }

    var audioDataCallback: AudioDataCallback? = null

    private fun wavHeader(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(44)

        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.putInt(0x46464952)
        byteBuffer.putInt(0)
        byteBuffer.putInt(0x45564157)
        byteBuffer.putInt(0x20746d66)
        byteBuffer.putInt(16)
        byteBuffer.putShort(1.toShort())
        byteBuffer.putShort(1.toShort())
        byteBuffer.putInt(44100)
        byteBuffer.putInt(44100 * 1 * 2)
        byteBuffer.putShort((1 * 2).toShort())
        byteBuffer.putShort(16.toShort())
        byteBuffer.putInt(0x61746164)
        byteBuffer.putInt(0)

        return byteBuffer.array()
    }

    private fun prepareWAVSize(mFilePath: String, mFileSize: Int) {
        val ras = RandomAccessFile(mFilePath, "rw")
        ras.seek(4)

        val byteBuffer = ByteBuffer.allocate(4)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.putInt(mFileSize - 8)
        ras.write(byteBuffer.array())

        byteBuffer.rewind()
        byteBuffer.putInt(mFileSize - 42)
        ras.seek(40)
        ras.write(byteBuffer.array())
    }
}

