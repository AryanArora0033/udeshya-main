package com.example.udeshya

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CameraActivity : AppCompatActivity() {
    lateinit var cameraDevice: CameraDevice
    private lateinit var textureView: TextureView
    private lateinit var cameraManager: CameraManager // Declare cameraManager as lateinit
    lateinit var handler: Handler
    val code = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera)

        // Initialize cameraManager
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        // Define handlerThread and handler
        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        // Function for camera permissions
        getpermission()



        // TextureView setup
        textureView = findViewById(R.id.texture_view)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                // No action needed
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

            }
        }
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    // Camera permissions
    private fun getpermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it from the user
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), code)
        } else {
            // Permission is granted, proceed with opening the camera
            open_camera()
        }
    }



    private fun open_camera() {
        val stateCallback = object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                // Camera opened successfully, you can proceed with camera operations
                cameraDevice = camera

                // Check if surfaceTexture is null
                if (textureView.isAvailable && textureView.surfaceTexture != null) {
                    val surfaceTexture = textureView.surfaceTexture
                    val surface = Surface(surfaceTexture)
                    val captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    captureRequest.addTarget(surface)
                    cameraDevice.createCaptureSession(listOf(surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                session.setRepeatingRequest(captureRequest.build(), null, null)
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                // Handle configuration failure
                            }
                        },
                        handler
                    )
                } else {
                    // Handle the case where surfaceTexture is not available or null
                    Log.e("CameraActivity", "TextureView surface or surfaceTexture is null or not available")
                }
            }

            override fun onDisconnected(camera: CameraDevice) {
                // Camera disconnected, handle cleanup or reconnection
            }

            override fun onError(camera: CameraDevice, error: Int) {
                // Handle any errors that occur with the camera device
            }
        }
        val cameraId = cameraManager.cameraIdList[0]
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            getpermission()
        }
        cameraManager.openCamera(cameraId, stateCallback, handler)
    }
}

