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
        userId = auth.currentUser?.uid ?: "default_user_id"

        // Menyiapkan RecyclerView
        recyclerView = view.findViewById(R.id.rvListChatBot)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatHistoryAdapter = ChatHistoryAdapter(chatHistoryList) { chatHistory ->
            // Menangani item yang diklik, bisa membuka percakapan lebih detail
            openChatDetail(chatHistory)
        }
        recyclerView.adapter = chatHistoryAdapter

        // Menginisialisasi Firebase Realtime Database
        database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).reference.child("chatbot").child(userId)

        // Mengambil data percakapan
        fetchChatHistory()

        val btnChat: FloatingActionButton = view.findViewById(R.id.btnChat)
        btnChat.setOnClickListener {
            // Intent untuk membuka Activity lain
            val intent = Intent(requireContext(), ChatViewActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun fetchChatHistory() {
        // Mengambil data percakapan dari Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatHistoryList.clear()
                for (chatSnapshot in snapshot.children) {
                    val chatId = chatSnapshot.key ?: continue
                    val lastMessage = chatSnapshot.child("lastMessage").value.toString()
                    val timestamp = chatSnapshot.child("timestamp").value.toString()

                    // Membuat objek ChatHistory dan menambahkannya ke daftar
                    val chatHistory = ChatHistory(chatId, lastMessage, timestamp)
                    chatHistoryList.add(chatHistory)
                }
                chatHistoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatBotFragment", "Failed to load chat history: ${error.message}")
            }
        })
    }

    private fun openChatDetail(chatHistory: ChatHistory) {
        // Membuka detail percakapan lebih lanjut, misalnya dengan Activity atau Fragment baru
        val intent = Intent(requireContext(), ChatViewActivity::class.java)
        intent.putExtra("CHAT_ID", chatHistory.chatId)
        startActivity(intent)
    }
}

