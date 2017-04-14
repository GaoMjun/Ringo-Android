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
import io.github.gaomjun.motionorientation.MotionOrientation
import io.github.gaomjun.motionorientation.MotionOrientation.DEVICE_ORIENTATION_LANDSCAPELEFT
import io.github.gaomjun.motionorientation.MotionOrientation.DEVICE_ORIENTATION_LANDSCAPERIGHT
import io.github.gaomjun.motionorientation.MotionOrientation.DEVICE_ORIENTATION_PORTRAIT
import io.github.gaomjun.motionorientation.MotionOrientation.DEVICE_ORIENTATION_UPSIDEDOWN
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity(), MotionOrientation.DeviceOrientationListener {
    private var cameraGLSurfaceView: CameraGLSurfaceView? = null
    private var glTextureView: GLTextureView? = null

    private var cameraEngine: CameraEngine? = null

    private var motionOrientation: MotionOrientation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        volumeControlStream = AudioManager.STREAM_MUSIC

        glTextureView = findViewById(R.id.glTextureView) as GLTextureView

        cameraEngine = CameraEngine.getInstance()
        cameraEngine!!.context = this

        findViewById(R.id.captureButton).setOnClickListener {

            // record action
//            if (glTextureView?.recording!!) {
//                glTextureView?.stopRecord(recordFinish = ::println)
//                Toast.makeText(applicationContext, "stop record", Toast.LENGTH_SHORT).show()
//            } else {
//                val cameraDirectory = File(Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DCIM + "/", "Camera")
//                val currentTimeString = SimpleDateFormat("yyyyMMddHHmmSS").format(Date())
//                val moviePath = cameraDirectory.absolutePath + "/" + currentTimeString + ".mp4"
//                glTextureView?.startRecord(moviePath)
//                Toast.makeText(applicationContext, "start record", Toast.LENGTH_SHORT).show()
//            }

            // capture action
            cameraEngine?.takePicture { data: ByteArray, camera: Camera ->
                    camera.startPreview()

                    val pictureSize = camera.parameters.pictureSize
                    OffScreenRenderer.render(applicationContext, data, pictureSize.width, pictureSize.height) {
                        bitmap: Bitmap ->
                        println("render finished")

                        val matrix = android.graphics.Matrix()
                        if (cameraEngine?.isFrontCamera!!) {
                            matrix.postScale(-1.0f, 1.0f)
                        }

                        when (MotionOrientation.DEVICE_ORIENTATION) {
                            DEVICE_ORIENTATION_PORTRAIT -> {
                                matrix.postRotate(270.0F)
                            }

                            DEVICE_ORIENTATION_UPSIDEDOWN -> {
                                matrix.postRotate(90.0F)
                            }

                            DEVICE_ORIENTATION_LANDSCAPERIGHT -> {
                                matrix.postRotate(180.0F)
                            }

                            DEVICE_ORIENTATION_LANDSCAPELEFT -> {
                            }

                            else -> {
                                matrix.postRotate(270.0F)
                            }
                        }

                        val bitmapFix = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//                        bitmap.recycle()

                        val ringoDirectory = File(Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DCIM + "/", "Camera")
                        saveBitmapToAlbum(bitmapFix, ringoDirectory.absolutePath) {
                            path ->
                            Log.d(TAG, path)
                            Toast.makeText(applicationContext, "save to " + path, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
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

        motionOrientation = MotionOrientation.init(this)
        motionOrientation?.deviceOrientationListener = this
    }

    override fun onPause() {
        super.onPause()

        if (glTextureView?.recording!!) {
            glTextureView?.stopRecord()
        }

        cameraEngine?.releaseCamera()

        motionOrientation?.releaseSensor()
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

    override fun deviceOrientationChanged(orientaion: Int) {
//        println(orientaion)
    }

    override fun deviceOrientationChangedFromTo(from: Int, to: Int) {
        println("$from, $to")
        if (to == DEVICE_ORIENTATION_PORTRAIT || to == DEVICE_ORIENTATION_UPSIDEDOWN || to == DEVICE_ORIENTATION_LANDSCAPERIGHT || to == DEVICE_ORIENTATION_LANDSCAPELEFT) {
            glTextureView?.orientation = to
        }
    }

    companion object {
        private val TAG = "MainActivity"
    }
}

