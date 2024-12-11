package com.example.capstonebangkitpawers.view

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.example.capstonebangkitpawers.R
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
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
import com.google.firebase.database.ValueEventListener
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="Halo aku Powie"

        etUserInput = findViewById(R.id.messageInput)
        rvChat = findViewById(R.id.messageRecyclerView)
        val btnSend: Button = findViewById(R.id.sendButton)
        val btnDelete: ImageButton = findViewById(R.id.btnDelete)

        val auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        chatId = intent.getStringExtra("CHAT_ID") ?: run {
            Toast.makeText(this, "Chat ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).reference.child("chatbot").child(userId).child(chatId)

        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        messageAdapter = MessageAdapter(messageList)
        rvChat.adapter = messageAdapter
        rvChat.setHasFixedSize(true)

        btnSend.setOnClickListener {
            newChat()
        }

        btnDelete.setOnClickListener {
            deleteChat()
        }

        if (chatId.isEmpty()) {
            chatId = database.push().key ?: UUID.randomUUID().toString()
            database.child("created_at").setValue(System.currentTimeMillis().toString())
        } else {
            fetchChatDetails()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

    }

    private fun newChat() {
        val userMessage = etUserInput.text.toString()
        if (userMessage.isNotEmpty()) {
            val timestamp = getCurrentTime()
            val message = Message("You", userMessage, timestamp, true)

            messageList.add(message)
            messageAdapter.notifyItemInserted(messageList.size - 1)
            rvChat.scrollToPosition(messageList.size - 1)
            etUserInput.text.clear()

            Log.d("ChatViewActivity", "User message: $userMessage")
            database.child("created_at").setValue(System.currentTimeMillis())


            saveMessageToFirebase(message)

            sendMessageToChatbot(userMessage)
        } else {
            Toast.makeText(this, "Please enter a message!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchChatDetails() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    messageList.clear()

                    createdAt = snapshot.child("created_at").value?.toString() ?: "0"

                    for (messageSnapshot in snapshot.child("messages").children) {
                        val content = messageSnapshot.child("content").value.toString()
                        val senderName = messageSnapshot.child("senderName").value.toString()
                        val timestamp = messageSnapshot.child("timestamp").value.toString()

                        val message = Message(senderName, content, timestamp, senderName == "You")
                        messageList.add(message)
                    }

                    messageAdapter.notifyDataSetChanged()
                    rvChat.scrollToPosition(messageList.size - 1)

                    if (createdAt != "0") {
                        val createdDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(createdAt.toLong()))
                        Log.d("ChatViewActivity", "Chat room created at: $createdDate")
                    } else {
                        Log.d("ChatViewActivity", "Created at timestamp is null or not available.")
                    }
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
        val messageId = database.child("messages").push().key
        if (messageId != null) {
            database.child("messages").child(messageId).setValue(message)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "Message saved successfully")
                    } else {
                        Log.d("Firebase", "Error saving message: ${task.exception}")
                    }
                }
        }
    }

    private fun sendMessageToChatbot(message: String) {
        val url = "https://chatbotmodel-347075362215.asia-southeast2.run.app/get_response"

        val json = JSONObject()
        json.put("message", message)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

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

                        val chatbotResponse = Message("Powie", chatbotMessage, timestamp, false)
                        messageList.add(chatbotResponse)
                        messageAdapter.notifyItemInserted(messageList.size - 1)
                        rvChat.scrollToPosition(messageList.size - 1)

                        saveMessageToFirebase(chatbotResponse)
                    } catch (e: Exception) {
                        Toast.makeText(this@ChatViewActivity, "Error parsing response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteChat() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin menghapus percakapan ini?")
            .setPositiveButton("Ya") { _, _ ->
                database.removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Percakapan berhasil dihapus", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Gagal menghapus percakapan", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }


}
