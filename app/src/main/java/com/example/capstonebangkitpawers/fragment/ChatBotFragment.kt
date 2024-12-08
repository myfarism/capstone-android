package com.example.capstonebangkitpawers.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.services.ChatHistory
import com.example.capstonebangkitpawers.services.Message
import com.example.capstonebangkitpawers.services.adapter.ChatHistoryAdapter
import com.example.capstonebangkitpawers.services.adapter.MessageAdapter
import com.example.capstonebangkitpawers.view.ChatViewActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatBotFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatHistoryAdapter: ChatHistoryAdapter
    private val chatHistoryList = mutableListOf<ChatHistory>()
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_bot, container, false)

        // Mendapatkan userId dari FirebaseAuth
        val auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        // Menyiapkan RecyclerView
        recyclerView = view.findViewById(R.id.rvListChatBot)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatHistoryAdapter = ChatHistoryAdapter(chatHistoryList) { chatHistory ->
            openChatDetail(chatHistory)  // Open existing chat
        }
        recyclerView.adapter = chatHistoryAdapter

        // Menginisialisasi Firebase Realtime Database
        database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).reference.child("chatbot").child(userId)

        // Mengambil data percakapan
        fetchChatHistory()

        val btnChat: FloatingActionButton = view.findViewById(R.id.btnChat)
        btnChat.setOnClickListener {
            // Membuat chatId baru untuk percakapan baru
            val newChatId = database.push().key ?: ""  // Membuat chatId baru menggunakan Firebase push key

            // Membuka ChatViewActivity dan mengirimkan chatId baru
            val intent = Intent(requireContext(), ChatViewActivity::class.java)
            intent.putExtra("CHAT_ID", newChatId)  // Kirimkan chatId baru
            startActivity(intent)
        }

        return view
    }

    private fun fetchChatHistory() {
        // Mengambil data percakapan dari Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatHistoryList.clear()  // Kosongkan daftar sebelum mengambil data baru
                if (snapshot.exists()) {
                    for (chatSnapshot in snapshot.children) {
                        val chatId = chatSnapshot.key ?: continue
                        val content = chatSnapshot.child("content").value.toString()
                        val senderName = chatSnapshot.child("senderName").value.toString()
                        val timestamp = chatSnapshot.child("timestamp").value.toString()

                        // Membuat objek ChatHistory dan menambahkannya ke daftar
                        val chatHistory = ChatHistory(chatId, userId, content, senderName, timestamp)
                        chatHistoryList.add(chatHistory)
                    }
                    // Memberi tahu adapter untuk memperbarui tampilan
                    chatHistoryAdapter.notifyDataSetChanged()
                } else {
                    Log.d("ChatBotFragment", "No chat history found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatBotFragment", "Failed to load chat history: ${error.message}")
            }
        })
    }

    private fun openChatDetail(chatHistory: ChatHistory) {
        // Membuka detail percakapan lebih lanjut
        val intent = Intent(requireContext(), ChatViewActivity::class.java)
        intent.putExtra("CHAT_ID", chatHistory.chatId)  // Kirimkan chatId dari history
        startActivity(intent)
    }
}

