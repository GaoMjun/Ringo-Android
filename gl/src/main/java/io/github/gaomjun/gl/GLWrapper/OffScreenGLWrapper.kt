package io.github.gaomjun.gl.GLWrapper

import io.github.gaomjun.gl.VertexArray

/**
 * Created by qq on 25/2/2017.
 */
class OffScreenGLWrapper(frameWidth: Int, frameHeight: Int) : GLWrapper() {
    var singleStepOffsetLocation: Int? = null
    var beautifiyLocation: Int? = null

    var width: Int = frameWidth
    var height: Int = frameHeight

    var frameBuffers: IntArray = IntArray(1)
    var frameBufferTextures: IntArray = IntArray(1)

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

        private val textureVertices = floatArrayOf(
                // [s t 0 1]
                0.0F, 1.0F, 0.0F, 1.0F,
                0.0F, 0.0F, 0.0F, 1.0F,
                1.0F, 0.0F, 0.0F, 1.0F,
                1.0F, 1.0F, 0.0F, 1.0F
        )
    }
}