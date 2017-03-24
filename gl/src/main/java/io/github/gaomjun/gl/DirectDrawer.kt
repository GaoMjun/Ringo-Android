package io.github.gaomjun.gl

import android.content.Context
import android.content.res.Configuration
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import io.github.gaomjun.cameraengine.CameraEngine
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import android.opengl.Matrix.multiplyMV
import io.github.gaomjun.gl.LocationHelper.Companion.getLocation

/**
 * Created by qq on 8/2/2017.
 */
class DirectDrawer(val textureID: Int, val context: Context) {

    private val squareCoords = floatArrayOf(
            -1.0F, 1.0F,
            -1.0F, -1.0F,
            1.0F, -1.0F,
            1.0F, 1.0F
    )

    private val squareCoords_PI_2 = floatArrayOf(
            -1.0F, -1.0F,
            1.0F, -1.0F,
            1.0F, 1.0F,
            -1.0F, 1.0F
    )

    private val squareCoords_PI = floatArrayOf(
            1.0F, -1.0F,
            1.0F, 1.0F,
            -1.0F, 1.0F,
            -1.0F, -1.0F
    )

    private val drawOrder = shortArrayOf(
            0, 1, 2,
            2, 3, 0
    )

    private val textureVertices = floatArrayOf(
            0.0F, 1.0F,
            1.0F, 1.0F,
            1.0F, 0.0F,
            0.0F, 0.0F
    )

    private val textureVertices_MIRROR_UD = floatArrayOf(
            0.0F, 0.0F,
            1.0F, 0.0F,
            1.0F, 1.0F,
            0.0F, 1.0F
    )

    private val textureVertices_MIRROR_LR = floatArrayOf(
            1.0F, 1.0F,
            0.0F, 1.0F,
            0.0F, 0.0F,
            1.0F, 0.0F
    )

    private val COORDS_PER_VERTEX = 2
    private val vertexStride = COORDS_PER_VERTEX * 4

    private var vertexBuffer: FloatBuffer? = null
    private var draqListBuffer: ShortBuffer? = null
    private var textureVerticesBuffer: FloatBuffer? = null

    private var glProgram: Int? = null

    init {
        var squareCoordsDefault: FloatArray?
        var textureVerticesDefault: FloatArray?

        when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                println("ORIENTATION_PORTRAIT")
                squareCoordsDefault = squareCoords

                if (CameraEngine.getInstance().isFrontCamera) {
                    println("front camera")
                    squareCoordsDefault = squareCoords_PI
                    textureVerticesDefault = textureVertices_MIRROR_UD
                } else {
                    println("back camera")
                    textureVerticesDefault = textureVertices
                }
            }

            else -> {
                println("ORIENTATION_LANDSCAPE")
                squareCoordsDefault = squareCoords_PI_2

                if (CameraEngine.getInstance().isFrontCamera) {
                    println("front camera")
                    textureVerticesDefault = textureVertices_MIRROR_LR
                } else {
                    println("back camera")
                    textureVerticesDefault = textureVertices
                }
            }
        }

        val vertexByteBuffer = ByteBuffer.allocateDirect(squareCoords.size * 4)
        vertexByteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = vertexByteBuffer.asFloatBuffer()
        vertexBuffer?.put(squareCoordsDefault)
        vertexBuffer?.position(0)

//        VertexArray(squareCoordsDefault)

        val draqListByteBuffer = ByteBuffer.allocateDirect(drawOrder.size * 2)
        draqListByteBuffer.order(ByteOrder.nativeOrder())
        draqListBuffer = draqListByteBuffer.asShortBuffer()
        draqListBuffer?.put(drawOrder)
        draqListBuffer?.position(0)

        val textureVerticesByteBuffer = ByteBuffer.allocateDirect(textureVertices.size * 4)
        textureVerticesByteBuffer.order(ByteOrder.nativeOrder())
        textureVerticesBuffer = textureVerticesByteBuffer.asFloatBuffer()
        textureVerticesBuffer?.put(textureVerticesDefault)
        textureVerticesBuffer?.position(0)

        compileShader()

        GLES20.glClearColor(0.5F, 0.5F, 0.5F, 1.0F)
    }

    private fun compileShader() {

        val vertexShaderCode = ShaderHelper.loadShaderCodeFromAssets(context, R.raw.vertex)
        val fragmentShaderCode = ShaderHelper.loadShaderCodeFromAssets(context, R.raw.fragment)
        val fragmentBeautyShaderCode = ShaderHelper.loadShaderCodeFromAssets(context, R.raw.fragment_beauty)

        val vertexShader = ShaderHelper.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode!!)
        val fragmentShader = ShaderHelper.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode!!)
        val fragmentBeautyShader = ShaderHelper.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentBeautyShaderCode!!)

        glProgram = ShaderHelper.linkProgram(vertexShader, fragmentBeautyShader)

        if (!ShaderHelper.validateProgram(glProgram!!)) {
            throw Exception("invalidateProgram")
        }
    }

    private val S_TEXTURE = "s_texture"
    private var sTextureLocation: Int? = null
    private val SINGLE_STEP_OFFSET = "singleStepOffset"
    private var singleStepOffsetLoction: Int? = null
    private val V_POSITION = "vPosition"
    private var vPositionLoction: Int? = null
    private val INPUT_TEXTURE_COORDINATE = "inputTextureCoordinate"
    private var inputTextureCoordinateLoction: Int? = null

    fun draw(transformMatrix: FloatArray, width: Int, height: Int) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUseProgram(glProgram!!)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID)

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        sTextureLocation = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, glProgram!!, S_TEXTURE)
        GLES20.glUniform1i(sTextureLocation!!, 0)

        singleStepOffsetLoction = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.UNIFORM, glProgram!!, SINGLE_STEP_OFFSET)
        GLES20.glUniform2fv(singleStepOffsetLoction!!, 1, floatArrayOf((2.0/width).toFloat(), (2.0/height).toFloat()), 0)

//        val transformMatrixHandler = GLES20.glGetUniformLocation(glProgram!!, "transformMatrix")
//        GLES20.glUniformMatrix4fv(transformMatrixHandler, 1, false, transformMatrix, 0)

        vPositionLoction = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.ATTRIBUTE, glProgram!!, V_POSITION)
        GLES20.glVertexAttribPointer(
                vPositionLoction!!, // This is the attribute location, and we pass in aPositionLocation to refer to the location that we retrieved
                COORDS_PER_VERTEX,  // This is the data count per attribute, or how many components are associated with each vertex for this attribute. we decided to use two floating point values per vertex: an x coordinate and a y coordinate to represent the position. This means that we have two components, and we had previously created the constant POSITION_COMPONENT_COUNT to contain that fact, so we pass that constant in here
                GLES20.GL_FLOAT,    // This is the type of data. We defined our data as a list of floating point values, so we pass in GL_FLOAT.
                false,              // This only applies if we use integer data, so we can safely ignore it for now.
                vertexStride,       // The fifth argument, the stride, applies when we store more than one attribute in a single array.
                vertexBuffer        // This tells OpenGL where to read the data.
        )
        GLES20.glEnableVertexAttribArray(vPositionLoction!!)

        inputTextureCoordinateLoction = LocationHelper.getLocation(LocationHelper.LOCATION_TYPE.ATTRIBUTE, glProgram!!, INPUT_TEXTURE_COORDINATE)
        GLES20.glVertexAttribPointer(inputTextureCoordinateLoction!!, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer)
        GLES20.glEnableVertexAttribArray(inputTextureCoordinateLoction!!)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, draqListBuffer)

        GLES20.glDisableVertexAttribArray(vPositionLoction!!)
        GLES20.glDisableVertexAttribArray(inputTextureCoordinateLoction!!)
    }
}