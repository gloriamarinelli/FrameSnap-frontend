package com.example.macc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: Button
    private lateinit var imageCapture: ImageCapture
    private val CAMERA_PERMISSION_REQUEST = 1001
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageView: ImageView
    private lateinit var selectedPaint: PaintBackend // Store selected paint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_fragment)
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        captureButton = findViewById(R.id.captureButton)
        imageView = findViewById(R.id.photoImageView)
        previewView = findViewById(R.id.previewView)

        // Retrieve selected paint from intent
        val paintId = intent.getIntExtra("paintId", -1)
        // Find the selected paint from the list based on the ID and assign it to selectedPaint
        // This logic should be implemented in PaintsFragment

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }

        val cameraPermission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), CAMERA_PERMISSION_REQUEST)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    @OptIn(ExperimentalGetImage::class)
    private fun openCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetRotation(previewView.display.rotation)
                .build()

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(previewView.display.rotation)
                .build()

            preview.setSurfaceProvider(previewView.surfaceProvider)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (e: Exception) {
                Log.e("openCamera()", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhotoWithPaint() {
        val imageCapture = imageCapture ?: return

        // Create output file to save captured photo
        val photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Photo capture success
                    val capturedPhotoBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                    // Overlay selected paint onto the captured photo
                    val bitmapWithPaint = overlayPaintOnPhoto(capturedPhotoBitmap, selectedPaint.paint)

                    // Display the modified photo with the paint overlay
                    imageView.setImageBitmap(bitmapWithPaint)
                }

                override fun onError(exception: ImageCaptureException) {
                    // Photo capture failure
                    Log.e("CameraFragment", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    // Function to overlay paint onto captured photo
    private fun overlayPaintOnPhoto(photoBitmap: Bitmap, paintBase64: String): Bitmap {
        // Decode base64 string to get paint bitmap
        val paintBytes = Base64.decode(paintBase64, Base64.DEFAULT)
        val paintBitmap = BitmapFactory.decodeByteArray(paintBytes, 0, paintBytes.size)

        // Resize paint bitmap to fit photo or adjust as necessary
        // Overlay paint bitmap onto photoBitmap at desired position
        // You can use Canvas and Paint to achieve this

        // Return the modified bitmap
        return photoBitmap // Placeholder, implement actual logic
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
    }
}
