package io.github.gaomjun.cmttracker;

/**
 * Created by qq on 10/11/2016.
 */

public class CMTTracker {

    public CMTTracker() {
    }

    public native void OpenCMT(long matAddrGr, int x1, int y1, int x2, int y2, boolean isFrontCamera);

    public native void ProcessCMT(long matAddrGr, boolean isFrontCamera);

    public static native int[] CMTgetRect();

    public static native boolean CMTgetResult();

    static {
        System.loadLibrary("cmt_tracker");
    }
}
