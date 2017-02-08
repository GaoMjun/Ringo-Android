package io.github.gaomjun.rtmpclient

/**
 * Created by qq on 3/2/2017.
 */

object RTMPNative {
    external fun CONNECT(publishUrl: String): Boolean
    external fun CLOSE()
    external fun SEND_METADATA(videoWidth: Int, videoHeight: Int, videoBitrate: Int, videoFps: Int,
                               audioSampleRate: Int, audioBitrate: Int)

    external fun SEND_PACKET(packetType: Int, data: ByteArray, size: Int, timestamp: Long)

    init {
        System.loadLibrary("rtmp")
        System.loadLibrary("rtmpnative")
    }
}
