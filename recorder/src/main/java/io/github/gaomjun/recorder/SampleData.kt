package io.github.gaomjun.recorder

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * Created by qq on 7/3/2017.
 */

data class SampleData(
        val byteBuffer: ByteBuffer,
        val mediaType: Int,
        val info: MediaCodec.BufferInfo
)