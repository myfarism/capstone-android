package com.example.capstonebangkitpawers.view

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.capstonebangkitpawers.R
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonebangkitpawers.services.Message
import com.example.capstonebangkitpawers.services.adapter.MessageAdapter
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewActivity : AppCompatActivity() {

    private lateinit var etUserInput: EditText
    private lateinit var rvChat: RecyclerView
    private val messageList = mutableListOf<Message>()
    private lateinit var messageAdapter: MessageAdapter
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_view)  // Use the correct layout file

        // Initialize views and set up RecyclerView
        etUserInput = findViewById(R.id.messageInput)
        rvChat = findViewById(R.id.messageRecyclerView)  // Initialize RecyclerView
        val btnSend: Button = findViewById(R.id.sendButton)

        // Setup RecyclerView and Adapter
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true  // Ini akan membuat RecyclerView dimulai dari bawah
            //reverseLayout = true // Optional: jika kamu ingin daftar pesan diatur dari bawah
        }
        messageAdapter = MessageAdapter(messageList)  // Pass mutable list
        rvChat.adapter = messageAdapter
        rvChat.setHasFixedSize(true)

        // Set onClickListener for the send button
        btnSend.setOnClickListener {
            val userMessage = etUserInput.text.toString()
            if (userMessage.isNotEmpty()) {
                // Send user message
                val timestamp = getCurrentTime()
                messageList.add(Message(userMessage, "You", timestamp, true)) // Add user message
                messageAdapter.notifyItemInserted(messageList.size - 1)
                rvChat.scrollToPosition(messageList.size - 1)
                etUserInput.text.clear()

                sendMessageToChatbot(userMessage)
            } else {
                Toast.makeText(this, "Please enter a message!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun sendMessageToChatbot(message: String) {
        val url = "https://chatbotmodel-347075362215.asia-southeast2.run.app/get_response"

        // Body request (JSON)
        val json = JSONObject()
        json.put("message", message)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        // Membuat request
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Menembak API
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ChatViewActivity, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseText)
                        val chatbotMessage = jsonResponse.getString("response")
                        val timestamp = getCurrentTime()
                        // Menambahkan pesan chatbot ke RecyclerView
                        messageList.add(Message(chatbotMessage, "Chatbot", timestamp, false))
                        messageAdapter.notifyItemInserted(messageList.size - 1)
                        rvChat.scrollToPosition(messageList.size - 1)
                    } catch (e: Exception) {
                        Toast.makeText(this@ChatViewActivity, "Error parsing response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Fungsi untuk mendapatkan waktu saat ini
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}
