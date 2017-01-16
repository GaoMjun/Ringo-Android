package io.github.gaomjun.cameraengine;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

/**
 * Created by qq on 27/12/2016.
 */

public class CameraGLSurfaceView extends GLSurfaceView {

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(2);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setRenderer(new CameraRenderer());
    }
}
