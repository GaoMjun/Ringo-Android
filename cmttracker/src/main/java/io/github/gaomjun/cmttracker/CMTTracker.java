package io.github.gaomjun.cmttracker;

/**
 * Created by qq on 10/11/2016.
 */

public class CMTTracker {

    public CMTTracker() {
    }

    public native void OpenCMT(long matAddrGr, int x, int y, int w, int h);

    public native void ProcessCMT(long matAddrGr);

    public static native int[] CMTgetRect();

    public static native boolean CMTgetResult();

    static {
        System.loadLibrary("cmt_tracker");
    }
}
