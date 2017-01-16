package io.github.gaomjun.yuvdecoder;

/**
 * Created by qq on 22/12/2016.
 */

public class YUVDecoder {
    public static native void YUVToARGB(byte[] yuv420sp, int width, int height, int[] argb);
    public static native void YUVToARGB(byte[] yuv420sp, int width, int height, byte[] argb);
    public static native void YUVToABGR(byte[] yuv420sp, int width, int height, int[] argb);

    static {
        System.loadLibrary("yuv_decoder");
    }
}
