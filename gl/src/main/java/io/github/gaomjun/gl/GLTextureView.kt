package io.github.gaomjun.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20.*
import android.opengl.Matrix
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.TextureView
import io.github.gaomjun.gl.Constants.Companion.OriginTransformMatrix
import io.github.gaomjun.gl.Constants.Companion.TEXTURE_COORDS_PER_VERTEX
import io.github.gaomjun.gl.Constants.Companion.TEXTURE_STRIDE
import io.github.gaomjun.gl.Constants.Companion.VERTEX_COORDS_PER_VERTEX
import io.github.gaomjun.gl.Constants.Companion.VERTEX_STRIDE
import io.github.gaomjun.gl.GLWrapper.GLWrapper
import io.github.gaomjun.gl.GLWrapper.MediaCodecGLWrapper
import io.github.gaomjun.gl.GLWrapper.OffScreenGLWrapper
import io.github.gaomjun.gl.GLWrapper.SurfaceGLWrapper
import io.github.gaomjun.glencoder.GLH264Encoder
import io.github.gaomjun.motionorientation.MotionOrientation.DEVICE_ORIENTATION_LANDSCAPELEFT
import io.github.gaomjun.motionorientation.MotionOrientation.DEVICE_ORIENTATION_LANDSCAPERIGHT
import io.github.gaomjun.motionorientation.MotionOrientation.DEVICE_ORIENTATION_PORTRAIT
import io.github.gaomjun.motionorientation.MotionOrientation.DEVICE_ORIENTATION_UNKNOWN
import io.github.gaomjun.motionorientation.MotionOrientation.DEVICE_ORIENTATION_UPSIDEDOWN

/**
 * Created by qq on 14/2/2017.
 */
class GLTextureView(context: Context?, attrs: AttributeSet?) : TextureView(context, attrs),
        TextureView.SurfaceTextureListener,
        SurfaceTexture.OnFrameAvailableListener {

    private var cameraGLWrapper: SurfaceGLWrapper? = null
    private var bufferGLWrapper: OffScreenGLWrapper? = null
    private var mediaCodecGLWrapper: MediaCodecGLWrapper? = null

    private var surface: SurfaceTexture? = null

    private var renderThread: HandlerThread? = null
    private var renderHandler: RenderHandler? = null

    private var frameWidth: Int = 0
    private var frameHeight: Int = 0

    var cameraTexture: SurfaceTexture? = null
    private var textureID: Int? = null

    var surfaceAvailableCallback: ((surfaceTexture: SurfaceTexture) -> Unit)? = null

    var recording = false

    var orientation: Int? = DEVICE_ORIENTATION_LANDSCAPERIGHT
    private var videoRotation = 0

    init {
        println("init")

        surfaceTextureListener = this
    }

    private fun initGL() {
        println("initGL")

        initBufferGLWrapper()

        initCameraGLWrapper()

//        initMediaCodecGLWrapper()

        textureID = TextureHelper.createTexture()
        cameraTexture = SurfaceTexture(textureID!!)
        cameraTexture?.setOnFrameAvailableListener(this)

        surfaceTextureCallback?.onTextureAvailable(cameraTexture!!)
        surfaceAvailableCallback?.invoke(cameraTexture!!)
    }

    private fun initMediaCodecGLWrapper(glH264Encoder: GLH264Encoder) {
        mediaCodecGLWrapper = MediaCodecGLWrapper(glH264Encoder)
        GLHelper.initGLWithWrapper(mediaCodecGLWrapper!!, bufferGLWrapper?.eglContext!!)
        GLHelper.makeGLCurrent(mediaCodecGLWrapper!!)
        mediaCodecGLWrapper?.glProgram = GLHelper.createGLProgram(
                ShaderHelper.loadShaderCodeFromAssets(context, R.raw.vertex2)!!,
                ShaderHelper.loadShaderCodeFromAssets(context, R.raw.fragment)!!
        )
        glUseProgram(mediaCodecGLWrapper?.glProgram!!)
        mediaCodecGLWrapper?.textureLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, mediaCodecGLWrapper?.glProgram!!, "s_texture")
        mediaCodecGLWrapper?.positionLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.ATTRIBUTE, mediaCodecGLWrapper?.glProgram!!, "vPosition")
        mediaCodecGLWrapper?.textureCoordinateLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.ATTRIBUTE, mediaCodecGLWrapper?.glProgram!!, "inputTextureCoordinate")
        mediaCodecGLWrapper?.transformMatrixLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, mediaCodecGLWrapper?.glProgram!!, "transformMatrix")
        glUseProgram(0)
    }

    private fun initCameraGLWrapper() {
        cameraGLWrapper = SurfaceGLWrapper(surface!!)
        GLHelper.initGLWithWrapper(cameraGLWrapper!!, bufferGLWrapper?.eglContext!!)
        GLHelper.makeGLCurrent(cameraGLWrapper!!)
        cameraGLWrapper?.glProgram = GLHelper.createGLProgram(
                ShaderHelper.loadShaderCodeFromAssets(context, R.raw.vertex2)!!,
                ShaderHelper.loadShaderCodeFromAssets(context, R.raw.fragment)!!
        )
        glUseProgram(cameraGLWrapper?.glProgram!!)
        cameraGLWrapper?.textureLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, cameraGLWrapper?.glProgram!!, "s_texture")
        cameraGLWrapper?.positionLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.ATTRIBUTE, cameraGLWrapper?.glProgram!!, "vPosition")
        cameraGLWrapper?.textureCoordinateLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.ATTRIBUTE, cameraGLWrapper?.glProgram!!, "inputTextureCoordinate")
        cameraGLWrapper?.transformMatrixLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, cameraGLWrapper?.glProgram!!, "transformMatrix")
        glUseProgram(0)
    }

    private fun initBufferGLWrapper() {
        bufferGLWrapper = OffScreenGLWrapper(frameWidth, frameHeight)
        GLHelper.initGLWithWrapper(bufferGLWrapper!!)
        GLHelper.makeGLCurrent(bufferGLWrapper!!)
        bufferGLWrapper?.glProgram = GLHelper.createGLProgram(
                ShaderHelper.loadShaderCodeFromAssets(context, R.raw.vertex2)!!,
                ShaderHelper.loadShaderCodeFromAssets(context, R.raw.fragment_beauty)!!
        )
        glUseProgram(bufferGLWrapper?.glProgram!!)
        bufferGLWrapper?.textureLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, bufferGLWrapper?.glProgram!!, "s_texture")
        bufferGLWrapper?.positionLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.ATTRIBUTE, bufferGLWrapper?.glProgram!!, "vPosition")
        bufferGLWrapper?.textureCoordinateLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.ATTRIBUTE, bufferGLWrapper?.glProgram!!, "inputTextureCoordinate")
        bufferGLWrapper?.singleStepOffsetLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, bufferGLWrapper?.glProgram!!, "singleStepOffset")
        bufferGLWrapper?.transformMatrixLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, bufferGLWrapper?.glProgram!!, "transformMatrix")
        glUseProgram(0)

        GLHelper.createFrameBuffer(bufferGLWrapper?.frameBuffers!!, bufferGLWrapper?.frameBufferTextures!!, frameWidth, frameHeight)
        println("createFrameBuffer ${bufferGLWrapper?.frameBuffers!![0]} ${bufferGLWrapper?.frameBufferTextures!![0]}")
    }

    private var moviePath: String? = null

    fun startRecord(path: String) {
        moviePath = path

        renderHandler?.removeMessages(WHAT_INIT_MEIDACODEC)
        renderHandler?.sendMessage(renderHandler?.obtainMessage(WHAT_INIT_MEIDACODEC))
    }

    fun stopRecord(recordStatusCallback: RecordStatusCallback? = null, recordFinish: ((path: String?) -> Unit)? = null) {
        recording = false
        mediaCodecGLWrapper?.glH264Encoder?.stop()
        renderHandler?.removeMessages(WHAT_UNINIT_MEIDACODEC)
        renderHandler?.sendMessage(renderHandler?.obtainMessage(WHAT_UNINIT_MEIDACODEC))
        recordStatusCallback?.recordFinish(moviePath)
        recordFinish?.invoke(moviePath)
    }

    interface RecordStatusCallback {
        fun recordFinish(path: String?)
    }

    private val WHAT_INIT = 0x0001
    private val WHAT_FRAME_REACH = 0x0010
    private val WHAT_INIT_MEIDACODEC = 0x0100
    private val WHAT_UNINIT_MEIDACODEC = 0x1000

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        println("onSurfaceTextureDestroyed")

        surfaceTextureCallback?.onSurfaceTextureDestroyed(surface)
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        println("onSurfaceTextureAvailable")
        this.surface = surface
        frameWidth = width
        frameHeight = height

        renderThread = HandlerThread("renderThread")
        renderThread?.start()
        renderHandler = RenderHandler(renderThread!!.looper)

        renderHandler?.removeMessages(WHAT_INIT)
        renderHandler?.sendMessage(renderHandler?.obtainMessage(WHAT_INIT))

        surfaceTextureCallback?.onSurfaceTextureAvailable(surface, width, height)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        println("onSurfaceTextureSizeChanged")

        frameWidth = width
        frameHeight = height

        glViewport(0, 0, width, height)

        surfaceTextureCallback?.onSurfaceTextureSizeChanged(surface, width, height)
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
//        println("onSurfaceTextureUpdated")
        surfaceTextureCallback?.onSurfaceTextureUpdated(surface)
    }

    override fun onFrameAvailable(cameraTexture: SurfaceTexture?) {
//        println("onFrameAvailable")
        renderHandler?.removeMessages(WHAT_FRAME_REACH)
        renderHandler?.sendMessage(renderHandler?.obtainMessage(WHAT_FRAME_REACH))
    }

    interface SurfaceTextureListener {
        fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean
        fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int)
        fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int)
        fun onSurfaceTextureUpdated(surface: SurfaceTexture?)
        fun onTextureAvailable(texture: SurfaceTexture)
    }

    var surfaceTextureCallback: SurfaceTextureListener? = null

    private inner class RenderHandler(looper: Looper?) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            when(msg?.what) {
                WHAT_INIT -> {
                    initGL()
                }

                WHAT_INIT_MEIDACODEC -> {
                    when (orientation) {
                        DEVICE_ORIENTATION_PORTRAIT -> {
                            videoRotation = 90
                            initMediaCodecGLWrapper(GLH264Encoder(frameHeight, frameWidth))
                        }

                        DEVICE_ORIENTATION_UPSIDEDOWN -> {
                            videoRotation = 270
                            initMediaCodecGLWrapper(GLH264Encoder(frameHeight, frameWidth))
                        }

                        DEVICE_ORIENTATION_LANDSCAPERIGHT -> {
                            videoRotation = 0
                            initMediaCodecGLWrapper(GLH264Encoder(frameWidth, frameHeight))
                        }

                        DEVICE_ORIENTATION_LANDSCAPELEFT -> {
                            videoRotation = 180
                            initMediaCodecGLWrapper(GLH264Encoder(frameWidth, frameHeight))
                        }
                    }

                    recording = true
                    mediaCodecGLWrapper?.glH264Encoder?.start(moviePath!!)
                }

                WHAT_UNINIT_MEIDACODEC -> {
                    if (mediaCodecGLWrapper != null) {
                        GLHelper.releaseEGL(mediaCodecGLWrapper as GLWrapper)
                        mediaCodecGLWrapper = null
                    }
                }

                WHAT_FRAME_REACH -> {
                    GLHelper.makeGLCurrent(bufferGLWrapper!!)
                    cameraTexture?.updateTexImage()
                    drawFrameBuffer(OriginTransformMatrix)

                    if (recording) {
                        if (GLHelper.makeGLCurrent(mediaCodecGLWrapper!!)) {
                            val m = FloatArray(16)
                            cameraTexture?.getTransformMatrix(m)

                            Matrix.rotateM(m, 0, videoRotation.toFloat(), 0.0F, 0.0F, 1.0F)
                            when (videoRotation) {
                                0 -> {

                                }

                                90 -> {
                                    Matrix.translateM(m, 0, 0.0F, -1.0F, 0.0F)
                                }

                                180 -> {
                                    Matrix.translateM(m, 0, -1.0F, -1.0F, 0.0F)
                                }

                                270 -> {
                                    Matrix.translateM(m, 0, -1.0F, 0.0F, 0.0F)
                                }
                            }
                            drawMediaCodec(m)
                        }
                    }

                    GLHelper.makeGLCurrent(cameraGLWrapper!!)
                    val transformMatrix = FloatArray(16)
                    cameraTexture?.getTransformMatrix(transformMatrix)
                    if (frameHeight >= frameWidth) {
                        Matrix.rotateM(transformMatrix, 0, 90.0F, 0.0F, 0.0F, 1.0F)
                        Matrix.translateM(transformMatrix, 0, 0.0F, -1.0F, 0.0F)
                    }
                    drawScrenn(transformMatrix)
                }
            }
        }
    }

    private fun drawMediaCodec(transformMatrix: FloatArray) {
//        println("drawMediaCodec")
        glUseProgram(mediaCodecGLWrapper?.glProgram!!)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, bufferGLWrapper?.frameBufferTextures!![0])

        glUniform1i(mediaCodecGLWrapper?.textureLocation!!, 0)
        glUniformMatrix4fv(mediaCodecGLWrapper?.transformMatrixLocation!!, 1, false, transformMatrix, 0)

        mediaCodecGLWrapper?.vPositionLocationVertex?.enableVertexAttribArray(0, mediaCodecGLWrapper?.positionLocation!!, VERTEX_COORDS_PER_VERTEX, VERTEX_STRIDE)
        mediaCodecGLWrapper?.inputTextureCoordinateLocationVertex?.enableVertexAttribArray(0, mediaCodecGLWrapper?.textureCoordinateLocation!!, TEXTURE_COORDS_PER_VERTEX, TEXTURE_STRIDE)

        mediaCodecGLWrapper?.draw()

        glFinish()
        glDisableVertexAttribArray(mediaCodecGLWrapper?.positionLocation!!)
        glDisableVertexAttribArray(mediaCodecGLWrapper?.textureCoordinateLocation!!)
        glBindTexture(GL_TEXTURE_2D, 0)
        glUseProgram(0)

        GLHelper.swapGLBuffer(mediaCodecGLWrapper!!)
    }

    private fun drawFrameBuffer(transformMatrix: FloatArray) {
//        println("drawFrameBuffer")
        glBindFramebuffer(GL_FRAMEBUFFER, bufferGLWrapper?.frameBuffers!![0])

        glUseProgram(bufferGLWrapper?.glProgram!!)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID!!)

        glUniform1i(bufferGLWrapper?.textureLocation!!, 0)
        glUniform2fv(bufferGLWrapper?.singleStepOffsetLocation!!, 1, floatArrayOf((2.0 / width).toFloat(), (2.0 / height).toFloat()), 0)
        glUniformMatrix4fv(bufferGLWrapper?.transformMatrixLocation!!, 1, false, transformMatrix, 0)

        bufferGLWrapper?.vPositionLocationVertex?.enableVertexAttribArray(0, bufferGLWrapper?.positionLocation!!, VERTEX_COORDS_PER_VERTEX, VERTEX_STRIDE)
        bufferGLWrapper?.inputTextureCoordinateLocationVertex?.enableVertexAttribArray(0, bufferGLWrapper?.textureCoordinateLocation!!, TEXTURE_COORDS_PER_VERTEX, TEXTURE_STRIDE)

        bufferGLWrapper?.draw()

        glFinish()
        glDisableVertexAttribArray(bufferGLWrapper?.positionLocation!!)
        glDisableVertexAttribArray(bufferGLWrapper?.textureCoordinateLocation!!)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glUseProgram(0)
    }

    private fun drawScrenn(transformMatrix: FloatArray) {
//        println("drawScrenn")
        glUseProgram(cameraGLWrapper?.glProgram!!)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, bufferGLWrapper?.frameBufferTextures!![0])

        glUniform1i(cameraGLWrapper?.textureLocation!!, 0)
        glUniformMatrix4fv(cameraGLWrapper?.transformMatrixLocation!!, 1, false, transformMatrix, 0)

        cameraGLWrapper?.vPositionLocationVertex?.enableVertexAttribArray(0, cameraGLWrapper?.positionLocation!!, VERTEX_COORDS_PER_VERTEX, VERTEX_STRIDE)
        cameraGLWrapper?.inputTextureCoordinateLocationVertex?.enableVertexAttribArray(0, cameraGLWrapper?.textureCoordinateLocation!!, TEXTURE_COORDS_PER_VERTEX, TEXTURE_STRIDE)

        cameraGLWrapper?.draw()

        glFinish()
        glDisableVertexAttribArray(cameraGLWrapper?.positionLocation!!)
        glDisableVertexAttribArray(cameraGLWrapper?.textureCoordinateLocation!!)
        glBindTexture(GL_TEXTURE_2D, 0)
        glUseProgram(0)

        GLHelper.swapGLBuffer(cameraGLWrapper!!)
    }
}