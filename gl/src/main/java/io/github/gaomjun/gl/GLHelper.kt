package io.github.gaomjun.gl

import android.opengl.EGL14.*
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.GLES20
import io.github.gaomjun.gl.GLWrapper.GLWrapper
import io.github.gaomjun.gl.GLWrapper.MediaCodecGLWrapper
import io.github.gaomjun.gl.GLWrapper.OffScreenGLWrapper
import io.github.gaomjun.gl.GLWrapper.SurfaceGLWrapper

/**
 * Created by qq on 25/2/2017.
 */
class GLHelper {
    companion object {

        fun initGLWithWrapper(wrapper: GLWrapper, sharedContext: EGLContext = EGL_NO_CONTEXT) {

            wrapper.eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY)

            val versions = IntArray(2)
            eglInitialize(wrapper.eglDisplay, versions, 0, versions, 1)

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
            eglChooseConfig(wrapper.eglDisplay, configSpec, 0, configs, 0, 1, configsCount, 0)

            val eglConfig = configs[0]

            val contextSpec = intArrayOf(
                    EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL_NONE
            )
            wrapper.eglContext = eglCreateContext(wrapper.eglDisplay, eglConfig, sharedContext, contextSpec, 0)
            val versionValue = IntArray(1)
            if (!eglQueryContext(wrapper.eglDisplay, wrapper.eglContext, EGL_CONTEXT_CLIENT_VERSION, versionValue, 0)) {
                throw RuntimeException("eglQueryContext failed!")
            } else {
                println("eglQueryContext ${versionValue[0]}")
            }

            when(wrapper) {
                is OffScreenGLWrapper -> {
                    val surfaceAttribs = intArrayOf(
                            EGL_WIDTH, wrapper.width,
                            EGL_HEIGHT, wrapper.height,
                            EGL_NONE
                    )
                    wrapper.eglSurface = eglCreatePbufferSurface(wrapper.eglDisplay, eglConfig, surfaceAttribs, 0)
                    val widthValue = IntArray(1)
                    val heightValue = IntArray(1)
                    if (!eglQuerySurface(wrapper.eglDisplay, wrapper.eglSurface, EGL_WIDTH, widthValue, 0) ||
                        !eglQuerySurface(wrapper.eglDisplay, wrapper.eglSurface, EGL_HEIGHT, heightValue, 0)) {
                        throw RuntimeException("eglQuerySurface failed!")
                    } else {
                        println("eglQuerySurface [${widthValue[0]} ${heightValue[0]}]")
                    }
                }

                is SurfaceGLWrapper -> {
                    val surfaceAttribs = intArrayOf(
                            EGL_NONE
                    )
                    wrapper.eglSurface = eglCreateWindowSurface(wrapper.eglDisplay, eglConfig, wrapper.surface, surfaceAttribs, 0)
                }

                is MediaCodecGLWrapper -> {
                    val surfaceAttribs = intArrayOf(
                            EGL_NONE
                    )
                    wrapper.eglSurface = eglCreateWindowSurface(wrapper.eglDisplay, eglConfig, wrapper.surface, surfaceAttribs, 0)
                }
            }

            if (wrapper.eglSurface == EGL_NO_SURFACE) {
                throw RuntimeException("eglCreateSurface failed!")
            }
        }

        fun makeGLCurrent(wrapper: GLWrapper): Boolean {
            return eglMakeCurrent(wrapper.eglDisplay, wrapper.eglSurface, wrapper.eglSurface, wrapper.eglContext)
        }

        fun releaseEGL(wrapper: GLWrapper) {
            eglMakeCurrent(wrapper.eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)

            eglDestroyContext(wrapper.eglDisplay, wrapper.eglContext)
            eglDestroySurface(wrapper.eglDisplay, wrapper.eglSurface)
            eglTerminate(wrapper.eglDisplay)

            wrapper.eglDisplay = EGL_NO_DISPLAY
            wrapper.eglSurface = EGL_NO_SURFACE
            wrapper.eglContext = EGL_NO_CONTEXT
        }

        fun swapGLBuffer(wrapper: GLWrapper) {
            if (!eglSwapBuffers(wrapper.eglDisplay, wrapper.eglSurface)) {
                throw RuntimeException("eglSwapBuffers failed!")
            }
        }

        fun createGLProgram(vertexShaderCode: String, fragmentShaderCode: String): Int? {

            val vertexShader = ShaderHelper.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentBeautyShader = ShaderHelper.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

            return ShaderHelper.linkProgram(vertexShader, fragmentBeautyShader)
        }

        fun createFrameBuffer(frameBuffers: IntArray, frameBufferTextures: IntArray, width: Int, height:  Int) {
            TextureHelper.createFrameBufferTexture(frameBuffers, frameBufferTextures, width, height)
        }
    }
}