package io.github.gaomjun.recorder

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * Created by qq on 14/4/2017.
 */
class AudioEngine : PCMCapture.PCMDataCallback, AACEncoder.AudioDataListener {
    private var pcmCapture: PCMCapture? = null
    private var aacEncoder: AACEncoder? = null

    private var encoding = false

    var saveToFile = false

    constructor(encoding: Boolean = false) {
        this.encoding = encoding

        if (encoding) {
            aacEncoder = AACEncoder()
        } else {
            pcmCapture = PCMCapture()
        }
    }

    constructor(encoding: Boolean = false, audioConfiguration: AudioConfiguration) {
        this.encoding = encoding

        if (encoding) {
            aacEncoder = AACEncoder(audioConfiguration)
        } else {
            pcmCapture = PCMCapture(audioConfiguration)
        }
    }

    fun start() {
        if (encoding) {
            aacEncoder?.saveAACToFile = saveToFile
            aacEncoder?.audioDataListener = this
            aacEncoder?.start()
        } else {
            pcmCapture?.savePCMToFile = saveToFile
            pcmCapture?.pcmDataCallback = this
            pcmCapture?.start()
        }
    }

    fun stop() {
        if (encoding) {
            aacEncoder?.audioDataListener = null
            aacEncoder?.stop()
        } else {
            pcmCapture?.pcmDataCallback = null
            pcmCapture?.stop()
        }
    }

    override fun onPCMData(data: ByteArray, size: Int, timestamp: Long) {
        audioDataListener?.onPCMData(data, size, timestamp)
    }

    override fun onAACData(byteBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        audioDataListener?.onAACData(byteBuffer, info)
    }

    interface AudioDataListener {
        fun onPCMData(data: ByteArray, size: Int, timestamp: Long) {}
        fun onAACData(byteBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {}
    }

    var audioDataListener: AudioDataListener? = null
}