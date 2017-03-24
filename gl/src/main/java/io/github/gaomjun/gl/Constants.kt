package io.github.gaomjun.gl

/**
 * Created by qq on 22/2/2017.
 */

class Constants {
    companion object {
        val BYTES_PER_FLOAT = 4
        val BYTES_PER_SHORT = 2
        val VERTEX_COORDS_PER_VERTEX = 3
        val TEXTURE_COORDS_PER_VERTEX = 4
        val VERTEX_STRIDE = VERTEX_COORDS_PER_VERTEX * BYTES_PER_FLOAT
        val TEXTURE_STRIDE = TEXTURE_COORDS_PER_VERTEX * BYTES_PER_FLOAT
    }
}