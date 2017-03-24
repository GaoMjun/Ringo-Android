package io.github.gaomjun.gl

import android.opengl.GLES20

/**
 * Created by qq on 24/2/2017.
 */
class LocationHelper {
    companion object {
        fun getLocation(type: LOCATION_TYPE, glProgram: Int, name: String): Int? {
            var location: Int? = null

            when(type) {
                LOCATION_TYPE.UNIFORM ->
                    location = GLES20.glGetUniformLocation(glProgram, name)
                LOCATION_TYPE.ATTRIBUTE ->
                    location = GLES20.glGetAttribLocation(glProgram, name)
            }

            return location
        }
    }

    enum class LOCATION_TYPE {
        UNIFORM,
        ATTRIBUTE
    }
}