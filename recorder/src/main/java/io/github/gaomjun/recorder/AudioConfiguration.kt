package io.github.gaomjun.recorder

/**
 * Created by qq on 14/4/2017.
 */

data class AudioConfiguration (

        val SAMPLE_RATE: Int = 44100,
        val CHANNEL_NUM: Int = 2,
        val BITS_PER_SAMPLE: Int = 16,
        val LENGTH_PER_READ: Int = 4096,

        val BITRATE: Int = 192000,
        val MAX_INPUT_SIZE: Int = 14208
)