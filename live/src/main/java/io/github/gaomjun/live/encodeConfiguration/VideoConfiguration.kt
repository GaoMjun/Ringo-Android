package io.github.gaomjun.live.encodeConfiguration

/**
 * Created by qq on 16/1/2017.
 */

class VideoConfiguration(var width: Int = 1280,
                         var height: Int = 720,
                         var bitrate: Int = 1000 * 1024,
                         var fps: Int = 10,
                         var keyframeInterval: Int = fps * 2) {

    companion object {
        @JvmStatic fun instance() = Holder.instance
    }

    private object Holder {
        val instance = VideoConfiguration()
    }
}
