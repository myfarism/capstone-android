package com.example.capstonebangkitpawers.view

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonebangkitpawers.R
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ScanActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var captureImageView: ImageView
    private lateinit var tflite: Interpreter
    private lateinit var labels: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        resultTextView = findViewById(R.id.resultTextView)
        captureImageView = findViewById(R.id.captureImageView)

        val imageUriString: String? = intent.getStringExtra("imageUri")
        val imageUri: Uri? = imageUriString?.let { Uri.parse(it) }
        captureImageView.setImageURI(imageUri)
        loadModel()

        if (imageUri != null) {
            classifyImage(imageUri)
        }
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
        val inputSize = 224  // Input size expected by the model
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

        // Resize the image to match the input size of the model
        return Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
    }

    private fun classifyImage(imageUri: Uri) {
        val inputBitmap = preprocessImage(imageUri)

        // Convert the Bitmap to a ByteBuffer that the model understands
        val byteBuffer = convertBitmapToByteBuffer(inputBitmap)

        // Prepare an output array for the results
        val output = Array(1) { FloatArray(labels.size) }

        // Run the model on the image data
        tflite.run(byteBuffer, output)

        // Find the label with the highest confidence
        val result = output[0]
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1
        val predictedLabel = labels[maxIndex]
        val confidence = result[maxIndex]

        displayResult(predictedLabel, confidence)  // Display the result
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputSize = 224
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        // Get pixel data from Bitmap and add to ByteBuffer
        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in intValues) {
            byteBuffer.putFloat((pixelValue shr 16 and 0xFF).toFloat())  // Red
            byteBuffer.putFloat((pixelValue shr 8 and 0xFF).toFloat())   // Green
            byteBuffer.putFloat((pixelValue and 0xFF).toFloat())         // Blue
        }
        return byteBuffer
    }

    private fun displayResult(label: String, confidence: Float) {
        runOnUiThread {
            val resultText = "Predicted: $label\nConfidence: ${confidence * 100}%"
            resultTextView.text = resultText
        }
    }
}
