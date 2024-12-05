package com.example.capstonebangkitpawers.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.databinding.ActivityScanBinding
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var tflite: Interpreter
    private lateinit var labels: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS)
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.captureImage.setOnClickListener {
            takePhoto()
        }

        binding.gallery.setOnClickListener {
            openGallery()
        }

        loadModel()
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Use case binding failed", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            outputDirectory,
            "captured_image.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(baseContext, "Photo capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    classifyImage(savedUri)
                }
            }
        )
    }

    private val outputDirectory: File by lazy {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            classifyImage(it)
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun loadModel() {
        val modelFile = File(filesDir, "model.tflite")
        assets.open("model.tflite").use { inputStream ->
            modelFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        tflite = Interpreter(modelFile)
        labels = loadLabels()
    }

    private fun loadLabels(): List<String> {
        return assets.open("labels.txt").bufferedReader().use { it.readLines() }
    }

    private fun preprocessImage(imageUri: Uri): Bitmap {
        val inputSize = 224
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

        return Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
    }

    private fun classifyImage(imageUri: Uri) {
        val inputBitmap = preprocessImage(imageUri)
        val byteBuffer = convertBitmapToByteBuffer(inputBitmap)
        val output = Array(1) { FloatArray(labels.size) }

        tflite.run(byteBuffer, output)

        val result = output[0]
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1
        val predictedLabel = labels[maxIndex]
        val confidence = result[maxIndex]

        sendResultToOutputActivity(imageUri, predictedLabel, confidence)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputSize = 224
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in intValues) {
            byteBuffer.putFloat((pixelValue shr 16 and 0xFF).toFloat())
            byteBuffer.putFloat((pixelValue shr 8 and 0xFF).toFloat())
            byteBuffer.putFloat((pixelValue and 0xFF).toFloat())
        }
        return byteBuffer
    }

    private fun sendResultToOutputActivity(imageUri: Uri, label: String, confidence: Float) {
        val intent = Intent(this, OutputActivity::class.java).apply {
            putExtra("imageUri", imageUri)
            putExtra("predictedLabel", label)
            putExtra("confidence", confidence)
        }
        startActivity(intent)
    }
}
