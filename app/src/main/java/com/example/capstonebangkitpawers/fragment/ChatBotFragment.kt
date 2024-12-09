package com.example.capstonebangkitpawers.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.services.ChatHistory
import com.example.capstonebangkitpawers.services.adapter.ChatHistoryAdapter
import com.example.capstonebangkitpawers.view.ChatViewActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

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

        val auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        recyclerView = view.findViewById(R.id.rvListChatBot)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatHistoryAdapter = ChatHistoryAdapter(chatHistoryList) { chatHistory ->
            openChatDetail(chatHistory)
        }
        recyclerView.adapter = chatHistoryAdapter

        database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).reference.child("chatbot").child(userId)

        fetchChatHistory()

        val btnChat: FloatingActionButton = view.findViewById(R.id.btnChat)
        btnChat.setOnClickListener {
            val chatId = UUID.randomUUID().toString()  // Buat chatId baru untuk percakapan baru
            val intent = Intent(requireContext(), ChatViewActivity::class.java)
            intent.putExtra("CHAT_ID", chatId)  // Kirimkan chatId ke activity
            startActivity(intent)
        }


        return view
    }


    private fun fetchChatHistory() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatHistoryList.clear()
                if (snapshot.exists()) {
                    for (chatSnapshot in snapshot.children) {
                        val chatId = chatSnapshot.key ?: continue
                        val content = chatSnapshot.child("content").value.toString()
                        val senderName = chatSnapshot.child("senderName").value.toString()

                        val timestampValue = chatSnapshot.child("timestamp").value
                        val timestamp = if (timestampValue is Long) {
                            timestampValue
                        } else {
                            System.currentTimeMillis()
                        }

                        val chatHistory = ChatHistory(chatId, userId, content, senderName, timestamp.toString())
                        chatHistoryList.add(chatHistory)
                    }
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
        val intent = Intent(requireContext(), ChatViewActivity::class.java)
        intent.putExtra("CHAT_ID", chatHistory.chatId)  // Kirimkan chatId dari history
        startActivity(intent)
    }
}

