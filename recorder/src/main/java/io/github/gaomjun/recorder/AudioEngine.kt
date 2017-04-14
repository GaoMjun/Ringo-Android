package io.github.gaomjun.recorder

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * Created by qq on 14/4/2017.
 */
class AudioEngine : PCMCapture.PCMDataCallback, AACEncoder.AACDataCallback {
    private var pcmCapture: PCMCapture? = null
    private var aacEncoder: AACEncoder? = null

    var encoding = false

    constructor() {
        pcmCapture = PCMCapture()
        aacEncoder = AACEncoder()
    }

    constructor(audioConfiguration: AudioConfiguration) {
        pcmCapture = PCMCapture(audioConfiguration)
        aacEncoder = AACEncoder(audioConfiguration)
    }

    fun start() {
        if (encoding) {
            aacEncoder?.aacDataCallback = this
            aacEncoder?.start()
        } else {
            pcmCapture?.pcmDataCallback = this
            pcmCapture?.start()
        }
    }

    fun stop() {
        if (encoding) {
            aacEncoder?.aacDataCallback = null
            aacEncoder?.stop()
        } else {
            pcmCapture?.pcmDataCallback = null
            pcmCapture?.stop()
        }
    }

    override fun onPCMData(data: ByteArray, size: Int, timestamp: Long) {
        pcmDataListener?.onPCMData(data, size, timestamp)
    }

    override fun onAACData(byteBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        aacDataListener?.onAACData(byteBuffer, info)
    }

    interface PCMDataListener {
        fun onPCMData(data: ByteArray, size: Int, timestamp: Long)
    }

    var pcmDataListener: PCMDataListener? = null

    interface AACDataListener {
        fun onAACData(byteBuffer: ByteBuffer, info: MediaCodec.BufferInfo)
    }

    var aacDataListener: AACDataListener? = null
}