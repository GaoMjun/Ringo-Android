package io.github.gaomjun.gl

import android.content.Context
import android.opengl.GLES20
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Created by qq on 21/2/2017.
 */
class ShaderHelper {
    companion object {
        fun loadShaderCodeFromAssets(context: Context, resId: Int): String? {
            val inputStream = context.resources.openRawResource(resId)

            val inputStreamReader = InputStreamReader(inputStream)

            val bufferedReader = BufferedReader(inputStreamReader)

            val stringBuilder = StringBuilder()

            do {
                val line = bufferedReader.readLine()

                if (line != null) {
                    stringBuilder.append(line)
                    stringBuilder.append('\n')
                }

            } while (line != null)

            inputStream.close()

            return stringBuilder.toString()
        }

        fun loadShader(shaderType: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(shaderType)

            GLES20.glShaderSource(shader, shaderCode)

            GLES20.glCompileShader(shader)

            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == GLES20.GL_FALSE) {
                throw RuntimeException("glCompileShader falied ${GLES20.glGetShaderInfoLog(shader)}")
            }

            return shader
        }

        fun linkProgram(vertexShader: Int, fragmentShader: Int): Int {
            val glProgram = GLES20.glCreateProgram()

            GLES20.glAttachShader(glProgram, vertexShader)
            GLES20.glAttachShader(glProgram, fragmentShader)

            GLES20.glLinkProgram(glProgram)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(glProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                throw RuntimeException("glLinkProgram failed")
            }

            return glProgram
        }

        fun validateProgram(glProgram: Int): Boolean {
            GLES20.glValidateProgram(glProgram)

            val validateStatus = IntArray(1)
            GLES20.glGetProgramiv(glProgram, GLES20.GL_VALIDATE_STATUS, validateStatus, 0)

            return validateStatus[0] != 0
        }
    }
}