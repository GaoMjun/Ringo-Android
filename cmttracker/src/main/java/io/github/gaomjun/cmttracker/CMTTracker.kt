package io.github.gaomjun.cmttracker

/**
 * Created by qq on 10/11/2016.
 */

class CMTTracker {

    external fun OpenCMT(matAddrGr: Long, x: Int, y: Int, w: Int, h: Int)

    external fun ProcessCMT(matAddrGr: Long, rect: IntArray): Boolean

    init {
        System.loadLibrary("cmt_tracker")
    }
}
