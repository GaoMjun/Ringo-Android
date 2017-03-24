package io.github.gaomjun.gl

import java.nio.ByteOrder.nativeOrder
import android.R.attr.order
import android.opengl.GLES20
import android.opengl.GLES20.*
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect
import java.nio.ByteOrder
import java.nio.FloatBuffer


/**
 * Created by qq on 2/3/2017.
 */

class Triangle {
    private val vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}"

    private val fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}"

    private val mProgram: Int

    private val vertexBuffer: FloatBuffer

    // Set color with red, green, blue and alpha (opacity) values
    internal var color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    init {
        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.size * 4)
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer()
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords)
        // set the buffer to read the first coordinate
        vertexBuffer.position(0)

        val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        mProgram = glCreateProgram()

        // add the vertex shader to program
        glAttachShader(mProgram, vertexShader)

        // add the fragment shader to program
        glAttachShader(mProgram, fragmentShader)

        // creates OpenGL ES program executables
        glLinkProgram(mProgram)
    }

    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount = triangleCoords.size / COORDS_PER_VERTEX
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw(frameBuffer: Int, texture: Int) {

        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer)

        // Add program to OpenGL ES environment
        glUseProgram(mProgram)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture)

        // get handle to vertex shader's vPosition member
        mPositionHandle = glGetAttribLocation(mProgram, "vPosition")

        // Enable a handle to the triangle vertices
        glEnableVertexAttribArray(mPositionHandle)

        // Prepare the triangle coordinate data
        glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GL_FLOAT, false,
                vertexStride, vertexBuffer)

        // get handle to fragment shader's vColor member
        mColorHandle = glGetUniformLocation(mProgram, "vColor")

        // Set color for drawing the triangle
        glUniform4fv(mColorHandle, 1, color, 0)

        // Draw the triangle
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        glFinish()
        glDisableVertexAttribArray(mPositionHandle)
        glBindTexture(GL_TEXTURE_2D, 0)
        glUseProgram(0)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    companion object {

        // number of coordinates per vertex in this array
        internal val COORDS_PER_VERTEX = 3
        internal var triangleCoords = floatArrayOf(// in counterclockwise order:
                0.0f, 0.622008459f, 0.0f, // top
                -0.5f, -0.311004243f, 0.0f, // bottom left
                0.5f, -0.311004243f, 0.0f  // bottom right
        )

        fun loadShader(type: Int, shaderCode: String): Int {

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            val shader = GLES20.glCreateShader(type)

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            return shader
        }
    }
}