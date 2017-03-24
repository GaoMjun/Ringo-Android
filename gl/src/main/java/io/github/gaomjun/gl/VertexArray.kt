package io.github.gaomjun.gl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import io.github.gaomjun.gl.Constants.Companion.BYTES_PER_FLOAT

/**
 * Created by qq on 22/2/2017.
 */
class VertexArray(vertexData: FloatArray) {
    private var floafBuffer: FloatBuffer? = null
    init {
        floafBuffer = ByteBuffer
                .allocateDirect(vertexData.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData)
    }

    fun enableVertexAttribArray(dataOffset: Int, attributeLocation: Int, componentCount: Int, stride: Int) {
        floafBuffer?.position(dataOffset)
        GLES20.glVertexAttribPointer(attributeLocation, componentCount, GLES20.GL_FLOAT, false, stride, floafBuffer)
        GLES20.glEnableVertexAttribArray(attributeLocation)

        floafBuffer?.position(0)
    }
}