package io.github.gaomjun.gl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.Matrix
import io.github.gaomjun.gl.Constants.Companion.TEXTURE_COORDS_PER_VERTEX
import io.github.gaomjun.gl.Constants.Companion.TEXTURE_STRIDE
import io.github.gaomjun.gl.Constants.Companion.VERTEX_COORDS_PER_VERTEX
import io.github.gaomjun.gl.Constants.Companion.VERTEX_STRIDE
import io.github.gaomjun.gl.GLWrapper.OffScreenGLWrapper
import java.nio.IntBuffer

/**
 * Created by qq on 9/3/2017.
 */
class OffScreenRenderer {
    companion object {
        @JvmStatic fun render(context: Context, data: ByteArray, width: Int, height: Int, callback: Callback? = null, renderFinish: ((bitmap: Bitmap) -> Unit)? = null) {
            Thread().run {
                val bitmapOrigin = BitmapFactory.decodeByteArray(data, 0, data.size)

                val bufferGLWrapper = OffScreenGLWrapper(width, height)
                GLHelper.initGLWithWrapper(bufferGLWrapper)
                GLHelper.makeGLCurrent(bufferGLWrapper)
                bufferGLWrapper.glProgram = GLHelper.createGLProgram(
                        ShaderHelper.loadShaderCodeFromAssets(context, R.raw.vertex2)!!,
                        ShaderHelper.loadShaderCodeFromAssets(context, R.raw.fragment_beauty2)!!
                )
                GLES20.glUseProgram(bufferGLWrapper.glProgram!!)
                bufferGLWrapper.textureLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, bufferGLWrapper.glProgram!!, "s_texture")
                bufferGLWrapper.positionLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.ATTRIBUTE, bufferGLWrapper.glProgram!!, "vPosition")
                bufferGLWrapper.textureCoordinateLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.ATTRIBUTE, bufferGLWrapper.glProgram!!, "inputTextureCoordinate")
                bufferGLWrapper.singleStepOffsetLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, bufferGLWrapper.glProgram!!, "singleStepOffset")
                bufferGLWrapper.transformMatrixLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, bufferGLWrapper.glProgram!!, "transformMatrix")
                GLES20.glUseProgram(0)
                bufferGLWrapper.frameBufferTextures[0] = TextureHelper.createTextureWithBitmap(bitmapOrigin)
                bitmapOrigin.recycle()
//                TextureHelper.createFrameBufferTextureWithImageData(bufferGLWrapper.frameBuffers, bufferGLWrapper.frameBufferTextures, width, height, data)

                GLHelper.makeGLCurrent(bufferGLWrapper)

                glUseProgram(bufferGLWrapper.glProgram!!)

                glActiveTexture(GL_TEXTURE0+bufferGLWrapper.frameBufferTextures[0])
                glBindTexture(GL_TEXTURE_2D, bufferGLWrapper.frameBufferTextures[0])

                glUniform1i(bufferGLWrapper.textureLocation!!, bufferGLWrapper.frameBufferTextures[0])
                glUniform2fv(bufferGLWrapper.singleStepOffsetLocation!!, 1, floatArrayOf((2.0 / width).toFloat(), (2.0 / height).toFloat()), 0)
                val transformMatrix = floatArrayOf(
                        -1.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, -1.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 1.0f, 0.0f,
                        1.0f, 1.0f, 0.0f, 1.0f
                )
                glUniformMatrix4fv(bufferGLWrapper.transformMatrixLocation!!, 1, false, transformMatrix, 0)

                bufferGLWrapper.vPositionLocationVertex?.enableVertexAttribArray(0, bufferGLWrapper.positionLocation!!, VERTEX_COORDS_PER_VERTEX, VERTEX_STRIDE)
                bufferGLWrapper.inputTextureCoordinateLocationVertex?.enableVertexAttribArray(0, bufferGLWrapper.textureCoordinateLocation!!, TEXTURE_COORDS_PER_VERTEX, TEXTURE_STRIDE)

                bufferGLWrapper.draw()

//                glBindFramebuffer(GL_FRAMEBUFFER, bufferGLWrapper.frameBuffers[0])

                val ABGRBuffer = IntBuffer.allocate(width * height)
                ABGRBuffer.position(0)
                glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, ABGRBuffer)
                val argbData = IntArray(ABGRBuffer.remaining())
                ColorConverter.ABGR2ARGB(ABGRBuffer.array(), argbData)
                val bitmap = Bitmap.createBitmap(argbData, width, height, Bitmap.Config.ARGB_8888)
                renderFinish?.invoke(bitmap)
                callback?.renderFinish(bitmap)

                glFinish()
                glDisableVertexAttribArray(bufferGLWrapper.positionLocation!!)
                glDisableVertexAttribArray(bufferGLWrapper.textureCoordinateLocation!!)
                glBindTexture(GL_TEXTURE_2D, 0)
//                glBindFramebuffer(GL_FRAMEBUFFER, 0)
                glUseProgram(0)
            }
        }

        interface Callback {
            fun renderFinish(bitmap: Bitmap)
        }
    }
}