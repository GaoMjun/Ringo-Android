package io.github.gaomjun.testcameraglpreview

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Toast
import io.github.gaomjun.cameraengine.CameraEngine
import io.github.gaomjun.extensions.postDelayedR
import io.github.gaomjun.gl.CameraGLSurfaceView
import io.github.gaomjun.gl.GLTextureView
import io.github.gaomjun.gl.OffScreenRenderer
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    private var cameraGLSurfaceView: CameraGLSurfaceView? = null
    private var glTextureView: GLTextureView? = null

    private var cameraEngine: CameraEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        volumeControlStream = AudioManager.STREAM_MUSIC

        glTextureView = findViewById(R.id.glTextureView) as GLTextureView

        cameraEngine = CameraEngine.getInstance()
        cameraEngine!!.context = this

        findViewById(R.id.captureButton).setOnClickListener {
            if (glTextureView?.recording!!) {
                glTextureView?.stopRecord(recordFinish = ::println)
            } else {
                val cameraDirectory = File(Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DCIM + "/", "Camera")
                val currentTimeString = SimpleDateFormat("yyyyMMddHHmmSS").format(Date())
                val moviePath = cameraDirectory.absolutePath + "/" + currentTimeString + ".mp4"
                glTextureView?.startRecord(moviePath)
            }

//            cameraEngine?.takePicture { data: ByteArray, camera: Camera ->
//                    camera.startPreview()
//
//                    val pictureSize = camera.parameters.pictureSize
//                    OffScreenRenderer.render(applicationContext, data, pictureSize.width, pictureSize.height) {
//                        bitmap: Bitmap ->
//                        println("render finished")
//
//                        val matrix = android.graphics.Matrix()
//                        if (cameraEngine?.isFrontCamera!!) {
//                            matrix.postScale(-1.0f, 1.0f)
//                        }
//                        if (glTextureView?.height!! > glTextureView?.width!!) {
//                            matrix.postRotate(-90.0f)
//                        } else {
//                            matrix.postRotate(180.0f)
//                        }
//                        val bitmapFix = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
////                        bitmap.recycle()
//
//                        val ringoDirectory = File(Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DCIM + "/", "Camera")
//                        saveBitmapToAlbum(bitmapFix, ringoDirectory.absolutePath) {
//                            path ->
//                            Log.d(TAG, path)
//                        }
//                    }
//            }
        }
    }

    override fun onResume() {
        super.onResume()

        cameraEngine?.openCamera(CameraEngine.CAMERA_FRONT)

        if (glTextureView?.cameraTexture != null) {
            cameraEngine?.startPreview(glTextureView?.cameraTexture)
        } else {
            glTextureView?.surfaceAvailableCallback = {
                surfaceTexture ->

                cameraEngine?.startPreview(surfaceTexture)
                println("surfaceChangedCallback")
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (glTextureView?.recording!!) {
            glTextureView?.stopRecord()
        }

        cameraEngine?.releaseCamera()
    }

    private var pressBackTimePre: Long = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                println("KEYCODE_BACK")
                val now = System.currentTimeMillis()
                if ((now - pressBackTimePre) > 1000) {
                    pressBackTimePre = now
                    Toast.makeText(applicationContext, "double click back to exit", Toast.LENGTH_SHORT).show()
                    return true
                } else {
                    Toast.makeText(applicationContext, "app exit", Toast.LENGTH_SHORT).show()
                    Handler().postDelayedR(1000) {
                        this.finish()
                        System.exit(0)
                    }
                    return super.onKeyDown(keyCode, event)
                }
            }

            else -> {
                return super.onKeyDown(keyCode, event)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    private fun saveBitmapToAlbum(bitmap: Bitmap, albumPath: String, succed: ((path: String) -> Unit)?) {
        val file = File(albumPath + "/", System.currentTimeMillis().toString() + ".jpg")

        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

            fileOutputStream.flush()
            fileOutputStream.close()
            bitmap.recycle()
            succed?.invoke(file.toString())

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = "MainActivity"
    }
}

