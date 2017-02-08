package io.github.gaomjun.live.encodeConfiguration

/**
 * Created by qq on 25/1/2017.
 */
class AudioConfiguration(var channels: Int = 1,
                         var samplerate: Int = 44100,
                         var bitrate: Int = 64000) {

    companion object {
        @JvmStatic fun instance() = Holder.instance
    }

    private object Holder {
        val instance = AudioConfiguration()
    }

}