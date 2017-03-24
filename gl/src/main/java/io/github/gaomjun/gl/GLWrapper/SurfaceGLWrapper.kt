package io.github.gaomjun.gl.GLWrapper

import android.graphics.SurfaceTexture
import io.github.gaomjun.gl.GLWrapper.GLWrapper
import io.github.gaomjun.gl.VertexArray

/**
 * Created by qq on 25/2/2017.
 */

class SurfaceGLWrapper(surface: SurfaceTexture) : GLWrapper() {
    val surface: SurfaceTexture? = surface

    init {
        vPositionLocationVertex = VertexArray(squareCoords)
        inputTextureCoordinateLocationVertex = VertexArray(textureVertices)
    }

    companion object {
        private val squareCoords = floatArrayOf(
                // [x y z]
                -1.0F, 1.0F, 0.0F,
                -1.0F, -1.0F, 0.0F,
                1.0F, -1.0F, 0.0F,
                1.0F, 1.0F, 0.0F
        )

        // back portrait
        private val squareCoords2 = floatArrayOf(
                -1.0F, 1.0F, 0.0F,
                1.0F, 1.0F, 0.0F,
                1.0F, -1.0F, 0.0F,
                -1.0F, -1.0F, 0.0F
        )

        // front portrait
        private val squareCoords3 = floatArrayOf(
                -1.0F, -1.0F, 0.0F,
                1.0F, -1.0F, 0.0F,
                1.0F, 1.0F, 0.0F,
                -1.0F, 1.0F, 0.0F
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