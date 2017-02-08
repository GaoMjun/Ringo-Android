package io.github.gaomjun.live.rtmpFrame

/**
 * Created by qq on 3/2/2017.
 */

open class RTMPFrame {
    var timestamp: Long? = null
    var data: ByteArray? = null
    open val packet: ByteArray? = null
    open val header: ByteArray? = null
    open val body: ByteArray? = null
}