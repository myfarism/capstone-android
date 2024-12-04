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
import java.io.IOException
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

        val imageUri: Uri? = intent.getParcelableExtra("imageUri")
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

    private fun preprocessImage(imageUri: Uri): ByteBuffer {
        val inputSize = 224
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputSize * inputSize)
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)
        for (pixelValue in intValues) {
            byteBuffer.putFloat(((pixelValue shr 16 and 0xFF) / 255.0f))
            byteBuffer.putFloat(((pixelValue shr 8 and 0xFF) / 255.0f))
            byteBuffer.putFloat(((pixelValue and 0xFF) / 255.0f))
        }

        return byteBuffer
    }

    private fun classifyImage(imageUri: Uri) {
        val inputBuffer = preprocessImage(imageUri)
        val output = Array(1) { FloatArray(labels.size) }
        tflite.run(inputBuffer, output)

        val result = output[0]
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1
        val predictedLabel = labels[maxIndex]
        val confidence = result[maxIndex]

        displayResult(predictedLabel, confidence)
    }

    private fun displayResult(label: String, confidence: Float) {
        runOnUiThread {
            val resultText = "Predicted: $label with confidence: ${"%.2f".format(confidence)}"
            resultTextView.text = resultText
        }
    }
}
