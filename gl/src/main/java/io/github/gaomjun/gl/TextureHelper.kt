package io.github.gaomjun.gl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLUtils
import android.opengl.GLUtils.*
import android.util.Log
import java.nio.ByteBuffer

/**
 * Created by qq on 22/2/2017.
 */
class TextureHelper {
    companion object {
        val TAG = "TextureHelper"

        fun loadTexture(context: Context, resourceId: Int): Int {

            val options = BitmapFactory.Options()
            options.inScaled = false
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

            return createTextureWithBitmap(bitmap)
        }

        fun createTextureWithBitmap(bitmap: Bitmap): Int {
            val textureObjectIds = IntArray(1)
            glGenTextures(1, textureObjectIds, 0)
            if (textureObjectIds[0] == 0) {
                throw RuntimeException("GLES20.glGenTextures failed")
            }

            glBindTexture(GL_TEXTURE_2D, textureObjectIds[0])

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)

            bitmap.recycle()

            glGenerateMipmap(GL_TEXTURE_2D)

            glBindTexture(GL_TEXTURE_2D, 0)

            return textureObjectIds[0]
        }

        fun createTexture(): Int {
            val textureObjectIds = IntArray(1)
            glGenTextures(1, textureObjectIds, 0)
            if (textureObjectIds[0] < 0) {
                Log.e(TAG, "GLES20.glGenTextures failed")

                return -1
            }

            glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureObjectIds[0])
            glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

            glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

            return textureObjectIds[0]
        }

        fun createFrameBufferTexture(frameBuffers: IntArray, frameBufferTextures: IntArray, width: Int, height: Int) {

            glGenTextures(1, frameBufferTextures, 0)
            glBindTexture(GL_TEXTURE_2D, frameBufferTextures[0])
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

            glGenFramebuffers(1, frameBuffers, 0)
            glBindFramebuffer(GL_FRAMEBUFFER, frameBuffers[0])
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, frameBufferTextures[0], 0)

            glBindTexture(GL_TEXTURE_2D, 0)
            glBindFramebuffer(GL_FRAMEBUFFER, 0)

            if (glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE) {
                println("createFrameBuffer [$width, $height] ${frameBuffers[0]} ${frameBufferTextures[0]}")
            }
        }

        fun createFrameBufferTextureWithImageData(frameBuffers: IntArray, frameBufferTextures: IntArray, width: Int, height: Int, data: ByteArray) {
            val dataBuffer = ByteBuffer.allocateDirect(data.size)
            dataBuffer.put(data)
            dataBuffer.position(0)

            glGenTextures(1, frameBufferTextures, 0)
            glBindTexture(GL_TEXTURE_2D, frameBufferTextures[0])
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, dataBuffer)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

            glGenFramebuffers(1, frameBuffers, 0)
            glBindFramebuffer(GL_FRAMEBUFFER, frameBuffers[0])
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, frameBufferTextures[0], 0)

            glBindTexture(GL_TEXTURE_2D, 0)
            glBindFramebuffer(GL_FRAMEBUFFER, 0)

            if (glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE) {
                println("createFrameBuffer [$width, $height] ${frameBuffers[0]} ${frameBufferTextures[0]}")
            }
        }
    }
}