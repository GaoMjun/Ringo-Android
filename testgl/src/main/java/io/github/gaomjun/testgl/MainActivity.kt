package io.github.gaomjun.testgl

import android.app.Activity
import android.graphics.SurfaceTexture
import android.opengl.*
import android.opengl.EGL14.*
import android.os.*
import android.view.TextureView

class MainActivity : Activity() {
    private var width = 0
    private var height = 0

    private var glThread: HandlerThread? = null
    private var glThreadHandler: GLThreadHandler? = null

    private fun initGLThread() {
        glThread = HandlerThread("glThread")
        glThread?.start()
        glThreadHandler = GLThreadHandler(glThread?.looper)
        glThreadHandler?.sendMessage(glThreadHandler?.obtainMessage(WHAT_INIT))
    }

    private var eglDisplay: EGLDisplay? = null
    private var eglContext: EGLContext? = null
    private var eglSurface: EGLSurface? = null

    private fun initEGL() {
        println("initEGL")

        eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY)

        val versions = IntArray(2)
        eglInitialize(eglDisplay, versions, 0, versions, 1)

        val configsCount = IntArray(1)
        val configs = arrayOfNulls<EGLConfig>(1)

        val configSpec = intArrayOf(
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                EGL_DEPTH_SIZE, 0,
                EGL_STENCIL_SIZE, 0,
                EGL_NONE
        )

        eglChooseConfig(eglDisplay, configSpec, 0, configs, 0, 1, configsCount, 0)

        val eglConfig = configs[0]

        val contextSpec = intArrayOf(
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
        )

        eglContext = eglCreateContext(eglDisplay, eglConfig, EGL_NO_CONTEXT, contextSpec, 0)

//        val surfaceAttribs = intArrayOf(
//                EGL_WIDTH, width,
//                EGL_HEIGHT, height,
//                EGL_NONE
//        )
//        eglSurface = eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs, 0)

        val surfaceAttribs = intArrayOf(
                EGL_NONE
        )
        eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, surfaceAttribs, 0)

        eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (findViewById(R.id.textureView) as TextureView)
                .surfaceTextureListener = SurfaceTextureListener()
    }

    private val WHAT_INIT = 0x01
    private val WHAT_DRAW = 0x10

    private inner class GLThreadHandler(looper: Looper?): Handler(looper) {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                WHAT_INIT -> {
                    initEGL()

                    glThreadHandler?.sendMessage(glThreadHandler?.obtainMessage(WHAT_DRAW))
                }

                WHAT_DRAW -> {
                    draw()
                }
            }
        }
    }

    private fun draw() {

    }

    private var surfaceTexture: SurfaceTexture? = null

    private inner class SurfaceTextureListener : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return true
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            surfaceTexture = surface
            initGLThread()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            this@MainActivity.width = width
            this@MainActivity.height = height
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }

    }
}




