package io.github.gaomjun.gl.GLWrapper

import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import io.github.gaomjun.gl.Constants.Companion.BYTES_PER_SHORT
import io.github.gaomjun.gl.VertexArray
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

/**
 * Created by qq on 25/2/2017.
 */
open class GLWrapper {
    var eglDisplay: EGLDisplay? = null
    var eglSurface: EGLSurface? = null
    var eglContext: EGLContext? = null

    var glProgram: Int? = null

    var textureLocation: Int? = null
    var positionLocation: Int? = null
    var textureCoordinateLocation: Int? = null

    var vPositionLocationVertex: VertexArray? = null
    var inputTextureCoordinateLocationVertex: VertexArray? = null

    fun draw() {
        GLES20.glClearColor(0.5F, 0.5F, 0.5F, 1.0F)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)
    }

    private var drawListBuffer: ShortBuffer? = null

    init {
        val draqListByteBuffer = ByteBuffer.allocateDirect(drawOrder.size * BYTES_PER_SHORT)
        draqListByteBuffer.order(ByteOrder.nativeOrder())
        drawListBuffer = draqListByteBuffer.asShortBuffer()
        drawListBuffer?.put(drawOrder)
        drawListBuffer?.position(0)
    }

    companion object {
        private val drawOrder = shortArrayOf(
                0, 1, 2,
                2, 3, 0
        )
    }
}