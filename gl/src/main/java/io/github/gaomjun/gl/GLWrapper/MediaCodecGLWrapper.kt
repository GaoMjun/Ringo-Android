package io.github.gaomjun.gl.GLWrapper

import android.view.Surface
import io.github.gaomjun.gl.VertexArray
import io.github.gaomjun.glencoder.GLH264Encoder

/**
 * Created by qq on 7/3/2017.
 */
class MediaCodecGLWrapper(h264Encoder: GLH264Encoder?) : GLWrapper() {
    val surface: Surface?

    var glH264Encoder: GLH264Encoder? = null

    init {
        vPositionLocationVertex = VertexArray(squareCoords)
        inputTextureCoordinateLocationVertex = VertexArray(textureVertices)

        glH264Encoder = h264Encoder

        surface = glH264Encoder?.inputSurface
    }

    companion object {
        private val squareCoords = floatArrayOf(
                // [x y z]
                -1.0F, 1.0F, 0.0F,
                -1.0F, -1.0F, 0.0F,
                1.0F, -1.0F, 0.0F,
                1.0F, 1.0F, 0.0F
        )

        private val textureVertices = floatArrayOf(
                // [s t 0 1]
                0.0F, 1.0F, 0.0F, 1.0F,
                0.0F, 0.0F, 0.0F, 1.0F,
                1.0F, 0.0F, 0.0F, 1.0F,
                1.0F, 1.0F, 0.0F, 1.0F
        )
    }
}