package io.github.gaomjun.cmttracker

import java.nio.ByteBuffer

/**
 * Created by qq on 10/11/2016.
 */

class CMTTracker {

    external fun OpenCMT(matAddrGr: Long, x: Int, y: Int, w: Int, h: Int)

    external fun ProcessCMT(matAddrGr: Long, rect: IntArray): Boolean

    external fun CMTInit(frameBuffer: ByteBuffer, frameWidth: Int, frameHeight: Int, x: Int, y: Int, w: Int, h: Int, isFront: Boolean)

    external fun CMTProcessing(frameBuffer: ByteBuffer, frameWidth: Int, frameHeight: Int, rect: IntArray, isFront: Boolean): Boolean

    init {
        System.loadLibrary("cmt_tracker")
    }
}
