package io.github.gaomjun.glencoder

import android.media.MediaCodec
import android.media.MediaCodec.*
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaFormat.*
import android.view.Surface
import io.github.gaomjun.recorder.Recorder

/**
 * Created by qq on 20/2/2017.
 */
class GLH264Encoder(frameWidth: Int, frameHeight: Int) {
    private val width = frameWidth
    private val height = frameHeight
    private val fps = 30
    private val bitrate = width * height * 3 * 2

    private var codec: MediaCodec? = null

    var inputSurface: Surface? = null

    private val recorder = Recorder()
    private var videoFormat: MediaFormat? = null
    private var videoFormatChanged: ((format: MediaFormat) -> Unit)? = null

    private var hasKeyframe = false

    init {
        initEncoder()
    }

    fun start(path: String) {
        hasKeyframe = false
//        codec?.start()

        if (videoFormat == null) {
            videoFormatChanged = {
                format: MediaFormat ->
                recorder.videoFormat = format
                recorder.start(path)
                videoFormatChanged = null
            }
        } else {
            recorder.videoFormat = videoFormat
            recorder.start(path)
        }
    }

    fun stop() {
//        codec?.stop()
        recorder.stop()
    }

    private fun initEncoder() {
        val format = createVideoFormat(MIMETYPE_VIDEO_AVC, width, height)
        format.setInteger(KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(KEY_BIT_RATE, bitrate)
        format.setInteger(KEY_FRAME_RATE, fps)
        format.setInteger(KEY_I_FRAME_INTERVAL, 1)

        codec = createEncoderByType(format.getString(KEY_MIME))
        codec?.setCallback(EncoderCallback())
        codec?.configure(format, null, null, CONFIGURE_FLAG_ENCODE)

        inputSurface = codec?.createInputSurface()

        codec?.start()
    }

    private fun releaseEncoder() {
        codec?.stop()
        codec?.release()
    }

    private inner class EncoderCallback : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec?, index: Int, info: MediaCodec.BufferInfo?) {
//            println("onOutputBufferAvailable $index ${info?.flags} ${info?.size} ${info?.presentationTimeUs}")
            if (recorder.recording) {
                if ((!hasKeyframe) &&
                        (info?.flags == BUFFER_FLAG_KEY_FRAME)) {
                    hasKeyframe = true
                }
                if (hasKeyframe) {
//                    println("onH264Data ${info?.size}")
                    recorder.muxing(codec?.getOutputBuffer(index)!!, Recorder.MEDIA_TYPE_VIDEO, info!!)
                }
            }
            codec?.releaseOutputBuffer(index, false)
        }

        override fun onOutputFormatChanged(codec: MediaCodec?, format: MediaFormat?) {
            println("onOutputFormatChanged")
            videoFormat = format
            videoFormatChanged?.invoke(format!!)
        }

        override fun onInputBufferAvailable(codec: MediaCodec?, index: Int) {
            println("onInputBufferAvailable")
        }

        override fun onError(codec: MediaCodec?, e: MediaCodec.CodecException?) {
            println("onError")
        }
    }
}


