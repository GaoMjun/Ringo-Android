package io.github.gaomjun.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import java.nio.ByteBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by qq on 8/2/2017.
 */
class CameraGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs) {

    private var textureID: Int? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var directDrawer: DirectDrawer? = null

    private var frameWidth: Int? = null
    private var frameHeight: Int? = null

    var surfaceChangedCallback: ((surfaceTexture: SurfaceTexture) -> Unit)? = null

    var pixelBuffer: IntBuffer? = null

    init {
        setEGLContextClientVersion(2)

        setRenderer(Renderer())

        renderMode = RENDERMODE_WHEN_DIRTY
    }

    private fun createTextureID(): Int {

        val texture = IntArray(1)

        GLES20.glGenTextures(1, texture, 0)

        return texture[0]
    }

    private inner class Renderer : GLSurfaceView.Renderer {
        override fun onDrawFrame(gl: GL10?) {
//            println("onDrawFrame")

            surfaceTexture!!.updateTexImage()

            val transformMatrix = FloatArray(16)
            surfaceTexture!!.getTransformMatrix(transformMatrix)

            directDrawer!!.draw(transformMatrix, frameWidth!!, frameHeight!!)

//            pixelBuffer?.position(0)
//            gl?.glReadPixels(0, 0, frameWidth!!, frameHeight!!, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixelBuffer)

//            sendDataToEncoder(pixelBuffer)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            println("onSurfaceChanged")

            frameWidth = width
            frameHeight = height

            pixelBuffer = IntBuffer.allocate(frameWidth!! * frameHeight!!)

            GLES20.glViewport(0, 0, width, height)

            surfaceChangedCallback?.invoke(surfaceTexture!!)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            println("onSurfaceCreated")

            textureID = createTextureID()
            println("createTextureID() $textureID")

            surfaceTexture = SurfaceTexture(textureID!!)

            surfaceTexture!!.setOnFrameAvailableListener {
//                println("onFrameAvailable")

                requestRender()
            }

            directDrawer = DirectDrawer(textureID!!, context)
        }
    }
}

















