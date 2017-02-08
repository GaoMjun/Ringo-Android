package io.github.gaomjun.live.rtmpClient

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import io.github.gaomjun.live.encodeConfiguration.AudioConfiguration
import io.github.gaomjun.live.encodeConfiguration.VideoConfiguration
import io.github.gaomjun.live.rtmpFrame.RTMPAudioFrame
import io.github.gaomjun.live.rtmpFrame.RTMPFrame
import io.github.gaomjun.live.rtmpFrame.RTMPVideoFrame
import io.github.gaomjun.rtmpclient.RTMPNative

/**
 * Created by qq on 3/2/2017.
 */

class RTMPClient {

    private val videoConfiguration = VideoConfiguration.instance()
    private val audioConfiguration = AudioConfiguration.instance()

    private val rtmpThread = HandlerThread("rtmpThread")
    private val rtmpThreadHandler: Handler

    private var hasKeyFrame = false
    private var startTime: Long = 0
    private var idle = false
    private var sendVideoHeader = true
    private var sendAudioHeader = true

    private val RTMP_PACKET_TYPE_VIDEO = 0x09
    private val RTMP_PACKET_TYPE_AUDIO = 0x08

    init {
        rtmpThread.start()

        rtmpThreadHandler = Handler(rtmpThread.looper)
    }

    companion object {
        @JvmStatic fun instance() = Holder.instance
    }

    private object Holder {
        val instance = RTMPClient()
    }

    interface ConnectStateCallback {
        fun connectState(state: Boolean)
    }

    fun connect(publish: String, connectStateCallback: ConnectStateCallback) {
        rtmpThreadHandler.post {
            if (RTMPNative.CONNECT(publish)) {
                sendMetaData()
                connectStateCallback.connectState(true)
            } else {
                connectStateCallback.connectState(false)
            }
        }
    }

    fun close() {
        rtmpThreadHandler.post {
            RTMPNative.CLOSE()
        }
    }

    private fun sendMetaData() {
        RTMPNative.SEND_METADATA(videoConfiguration.width, videoConfiguration.height, videoConfiguration.bitrate, videoConfiguration.fps,
                audioConfiguration.samplerate, audioConfiguration.bitrate)
    }

    fun sendFrame(frame: RTMPFrame) {
        if (!hasKeyFrame) {
            if ((frame is RTMPVideoFrame) && (frame.isKeyFrame!!)) {
                hasKeyFrame = true
            } else {
                return
            }
        }

        val frame = calculateTimestamp(frame)

        if (!idle) {
            when (frame) {
                is RTMPVideoFrame -> sendVideoFrame(frame)
                is RTMPAudioFrame -> sendAudioFrame(frame)
            }
        }
    }

    private fun sendAudioFrame(frame: RTMPAudioFrame) {
        if (sendAudioHeader) {
            sendAudioHeader = false

            println("sendAudioFrameHeader")
            sendPacket(RTMP_PACKET_TYPE_AUDIO, frame.header!!, 0)
        } else {
            println("sendAudioFrame")
            sendPacket(RTMP_PACKET_TYPE_AUDIO, frame.body!!, frame.timestamp!!)
        }
    }

    private fun sendVideoFrame(frame: RTMPVideoFrame) {
        if (sendVideoHeader) {
            if (frame.sps != null) {
                sendVideoHeader = false
                println("sendVideoFrameHeader")
                sendPacket(RTMP_PACKET_TYPE_VIDEO, frame.header!!, 0)
            } else {
                return
            }

        } else {
            println("sendVideoFrame")
            sendPacket(RTMP_PACKET_TYPE_VIDEO, frame.body!!, frame.timestamp!!)
        }
    }

    private fun sendPacket(packetType: Int, data: ByteArray, timestamp: Long) {
        rtmpThreadHandler.post {
            RTMPNative.SEND_PACKET(packetType, data, data.size, timestamp)
        }
    }

    private fun calculateTimestamp(frame: RTMPFrame): RTMPFrame {
        if (startTime == 0.toLong()) {
            startTime = frame.timestamp!!
        }

        frame.timestamp = frame.timestamp?.minus(startTime)

        return frame
    }
}