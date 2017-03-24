package io.github.gaomjun.gl

/**
 * Created by qq on 13/3/2017.
 */

object ColorConverter {
    init {
        System.loadLibrary("color_converter")
    }

    external fun RGBA2ARGB(rgba: IntArray, argb: IntArray)
    external fun ARGB2RGBA(argb: IntArray, rgba: IntArray)
    external fun ABGR2ARGB(abgr: IntArray, argb: IntArray)
}
