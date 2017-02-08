package io.github.gaomjun.libyuv;

/**
 * Created by qq on 20/1/2017.
 */

public class YUVNative {
    public static native void SCALE(byte[] i_yuv, int i_width, int i_height, byte[] o_yuv, int o_width, int o_height);
    public static native void ROTATE(byte[] i_yuv, int i_width, int i_height, int degree, byte[] o_yuv);
    public static native void NV21ToI420(byte[] i_nv21, int i_width, int i_height, byte[] o_i420);
    public static native void CONVERT(byte[] i_nv21, int i_width, int i_height, byte[] o_i420, int o_width, int o_height);

    static {
        System.loadLibrary("yuv");
        System.loadLibrary("yuvnative");
    }
}
