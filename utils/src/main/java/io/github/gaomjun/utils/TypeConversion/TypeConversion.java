package io.github.gaomjun.utils.TypeConversion;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * Created by qq on 2/12/2016.
 */

public final class TypeConversion {

    @NonNull
    public final static byte[] intToBytes(int num) {
        return ByteBuffer.allocate(4).putInt(num).array();
    }
}
