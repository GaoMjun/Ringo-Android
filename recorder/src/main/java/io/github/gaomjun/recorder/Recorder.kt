package io.github.gaomjun.recorder

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
import android.nfc.Tag
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import java.nio.ByteBuffer

/**
 * Created by qq on 7/3/2017.
 */
class Recorder : AACEncoder.AudioDataListener {

    private var recorderThead: HandlerThread? = null
    private var recorderThreadHandler: RecorderThreadHandler? = null

    private var mediaMuxer: MediaMuxer? = null

    var audioFormat: MediaFormat? = null
    var videoFormat: MediaFormat? = null

    private var audioTrackIndex: Int? = null
    private var videoTrackIndex: Int? = null

    var recording = false

    private val aacEncoder = AACEncoder()

    init {
        recorderThead = HandlerThread("recorderThead")
        recorderThead?.start()
        recorderThreadHandler = RecorderThreadHandler(recorderThead?.looper)
    }

    private var path: String? = null

    fun start(path: String) {
        Log.d(TAG, "start() " + path)
        audioTimestampStart = -1
        videoTimestampStart = -1
        this.path = path

        if (aacEncoder.audioFormat == null) {
            aacEncoder.start {
                format ->
                audioFormat = format

                recorderThreadHandler?.removeMessages(WHAT_INIT)
                recorderThreadHandler?.sendMessage(recorderThreadHandler?.obtainMessage(WHAT_INIT))
            }

        } else {
            aacEncoder.start()
            audioFormat = aacEncoder.audioFormat
            recorderThreadHandler?.removeMessages(WHAT_INIT)
            recorderThreadHandler?.sendMessage(recorderThreadHandler?.obtainMessage(WHAT_INIT))
        }
    }

    fun stop() {
        Log.d(TAG, "stop()")
        recording = false

        aacEncoder.stop()

        recorderThreadHandler?.removeMessages(WHAT_STOP)
        recorderThreadHandler?.sendMessage(recorderThreadHandler?.obtainMessage(WHAT_STOP))

    }

    private var audioTimestampStart: Long = -1
    private var videoTimestampStart: Long = -1

    fun muxing(byteBuffer: ByteBuffer, mediaType: Int, info: MediaCodec.BufferInfo) {
        when (mediaType) {
            MEDIA_TYPE_AUDIO -> {
                if (videoTimestampStart < 0) return

                if (audioTimestampStart < 0) {
                    audioTimestampStart = info.presentationTimeUs
                }

                info.presentationTimeUs = (info.presentationTimeUs - audioTimestampStart) / 1000
                info.presentationTimeUs += 6000
//                println("muxing audio ${info.presentationTimeUs}")
            }
            MEDIA_TYPE_VIDEO -> {
                if (videoTimestampStart < 0) {
                    videoTimestampStart = info.presentationTimeUs
                }
                info.presentationTimeUs = info.presentationTimeUs - videoTimestampStart
//                println("muxing video ${info.presentationTimeUs}")
            }
        }

        recorderThreadHandler?.removeMessages(WHAT_RECORDING)
        recorderThreadHandler?.sendMessage(recorderThreadHandler?.obtainMessage(WHAT_RECORDING, SampleData(byteBuffer, mediaType, info)))
    }

    private val WHAT_INIT = 0x0001
    private val WHAT_STOP = 0x0010
    private val WHAT_RECORDING = 0x0100

    private inner class RecorderThreadHandler(looper: Looper?) : Handler(looper) {

        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                WHAT_INIT -> {
                    initRecorder()
                }

                WHAT_STOP -> {
                    mediaMuxer?.stop()
                    mediaMuxer?.release()

                    audioFormat = null
                    videoFormat = null

                    audioTrackIndex = null
                    videoTrackIndex = null
                }

                WHAT_RECORDING -> {
                    val Obj = msg.obj
                    if (Obj is SampleData) {
                        val byteBuffer = Obj.byteBuffer
                        val mediaType = Obj.mediaType
                        val info = Obj.info

                        when (mediaType) {
                            MEDIA_TYPE_AUDIO -> {
                                if (audioTrackIndex != null) {
                                    mediaMuxer?.writeSampleData(audioTrackIndex!!, byteBuffer, info)
//                                    Log.d(TAG, "writeSampleData audioTrack")
                                } else {
                                    Log.w(TAG, "no audio track")
                                }
                            }

                            MEDIA_TYPE_VIDEO -> {
                                if (videoTrackIndex != null) {
                                    mediaMuxer?.writeSampleData(videoTrackIndex!!, byteBuffer, info)
//                                    Log.d(TAG, "writeSampleData videoTrack")
                                } else {
                                    Log.w(TAG, "no video track")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initRecorder() {
        println("initRecorder")
        mediaMuxer = MediaMuxer(path, MUXER_OUTPUT_MPEG_4)

        if ((audioFormat == null) && (videoFormat == null)) {
            throw Exception("mediaMuxer need at least one audio or video track")
        }

        if (audioFormat != null) {
            audioTrackIndex = mediaMuxer?.addTrack(audioFormat)
        }

        if (videoFormat != null) {
            videoTrackIndex = mediaMuxer?.addTrack(videoFormat)
        }

        mediaMuxer?.start()
        recording = true

        aacEncoder.audioDataListener = this
    }

    override fun onAACData(byteBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
//        println("onAACData ${info.size}")

        muxing(byteBuffer, MEDIA_TYPE_AUDIO, info)
    }

    companion object {
        val MEDIA_TYPE_AUDIO = 0
        val MEDIA_TYPE_VIDEO = 1

        private val TAG = "Recorder"
    }
}

