package com.example.capstonebangkitpawers.view

import android.os.Bundle
import android.util.Log
import com.example.capstonebangkitpawers.R
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.services.Message
import com.example.capstonebangkitpawers.services.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
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
    private lateinit var database: DatabaseReference
    private lateinit var userId: String
    private lateinit var chatId: String
    private lateinit var createdAt: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_view)

        // ToolBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize views and set up RecyclerView
        etUserInput = findViewById(R.id.messageInput)
        rvChat = findViewById(R.id.messageRecyclerView)
        val btnSend: Button = findViewById(R.id.sendButton)

        // Initialize Firebase Authentication and get userId
        val auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""  // Use Firebase Auth user ID or a fallback

        // Generate a unique chatId (this could be generated or passed from the previous screen)
        chatId = intent.getStringExtra("CHAT_ID") ?: return

        // Initialize Firebase Realtime Database reference
        database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).reference.child("chatbot").child(userId).child(chatId)

        // Check if this is the first time the chat is created
        checkAndCreateChatRoom()

        // Setup RecyclerView and Adapter
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true  // This will make the RecyclerView start from the bottom
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
                val message = Message("You", userMessage, timestamp, true)

                // Add message to the list and update RecyclerView
                messageList.add(message)
                messageAdapter.notifyItemInserted(messageList.size - 1)
                rvChat.scrollToPosition(messageList.size - 1)
                etUserInput.text.clear()

                // Save user message to Firebase
                saveMessageToFirebase(message)

                // Send the message to chatbot
                sendMessageToChatbot(userMessage)
            } else {
                Toast.makeText(this, "Please enter a message!", Toast.LENGTH_SHORT).show()
            }
        }

        if (chatId.isNotEmpty()) {
            fetchChatDetails()
        }
    }

    // Function to check if the chat room exists and create it with a timestamp if necessary
    private fun checkAndCreateChatRoom() {
        val chatRoomRef = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).reference.child("chatbot").child(userId).child(chatId)

        // Check if the room already exists
        chatRoomRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                if (!snapshot.exists()) {
                    // Room does not exist, so create it with a timestamp
                    val timestamp = ServerValue.TIMESTAMP
                    val chatRoomData = mapOf(
                        "created_at" to timestamp,
                        "messages" to mapOf<String, Any>()  // Messages initially empty
                    )

                    chatRoomRef.setValue(chatRoomData).addOnCompleteListener { createTask ->
                        if (createTask.isSuccessful) {
                            Log.d("ChatViewActivity", "Chat room created with timestamp.")
                        } else {
                            Log.e("ChatViewActivity", "Failed to create chat room: ${createTask.exception}")
                        }
                    }
                }
            } else {
                Log.e("ChatViewActivity", "Failed to check if room exists: ${task.exception}")
            }
        }
    }

    private fun fetchChatDetails() {
        // Fetch messages and created_at timestamp from Firebase based on chatId
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Clear previous messages
                    messageList.clear()

                    // Get created_at timestamp
                    createdAt = snapshot.child("created_at").value.toString()

                    // Iterate through all messages in the conversation
                    for (messageSnapshot in snapshot.child("messages").children) {
                        val content = messageSnapshot.child("content").value.toString()
                        val senderName = messageSnapshot.child("senderName").value.toString()
                        val timestamp = messageSnapshot.child("timestamp").value.toString()

                        // Create a new message and add it to the list
                        val message = Message(senderName, content, timestamp, senderName == "You")
                        messageList.add(message)
                    }

                    // Notify the adapter to refresh the RecyclerView
                    messageAdapter.notifyDataSetChanged()
                    rvChat.scrollToPosition(messageList.size - 1)

                    // Display the creation timestamp of the chat room
                    val createdDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(createdAt.toLong()))
                    Log.d("ChatViewActivity", "Chat room created at: $createdDate")
                } else {
                    Log.d("ChatViewActivity", "No messages found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewActivity", "Failed to load messages: ${error.message}")
            }
        })
    }

    private fun saveMessageToFirebase(message: Message) {
        val messageId = database.child("messages").push().key  // Generate unique key for each message
        if (messageId != null) {
            database.child("messages").child(messageId).setValue(message)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Message saved successfully
                        Log.d("Firebase", "Message saved successfully")
                    } else {
                        Log.d("Firebase", "Error saving message: ${task.exception}")
                    }
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
                        val chatbotResponse = Message("Powie", chatbotMessage, timestamp, false)
                        messageList.add(chatbotResponse)
                        messageAdapter.notifyItemInserted(messageList.size - 1)
                        rvChat.scrollToPosition(messageList.size - 1)

                        // Save chatbot response to Firebase
                        saveMessageToFirebase(chatbotResponse)
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
