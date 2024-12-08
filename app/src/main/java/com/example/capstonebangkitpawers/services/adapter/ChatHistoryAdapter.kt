package com.example.capstonebangkitpawers.services.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.services.ChatHistory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatHistoryAdapter(
    private val chatHistoryList: MutableList<ChatHistory>,  // Using MutableList for modification
    private val onItemClick: (ChatHistory) -> Unit
) : RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder>() {

    // Store the positions to be removed
    private val positionsToRemove = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chatbot, parent, false)
        return ChatHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHistoryViewHolder, position: Int) {
        val chatHistory = chatHistoryList[position]

        // Cek apakah chatId tidak kosong dan memiliki pesan
        if (chatHistory.chatId.isNotEmpty()) {
            checkMessagesExistence(chatHistory.userId, chatHistory.chatId) { hasMessages ->
                if (hasMessages) {
                    // Hanya tampilkan jika chat memiliki pesan
                    holder.bind(chatHistory)
                } else {
                    // Jangan tampilkan chat kosong dan hapus dari list
                    removeChatFromFirebase(chatHistory.userId, chatHistory.chatId)
                    chatHistoryList.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        } else {
            holder.itemView.visibility = View.GONE // Sembunyikan item jika tidak ada chatId
        }
    }


    override fun getItemCount(): Int {
        return chatHistoryList.size
    }

    // This method will be used to remove items once data is checked
    fun removeItems() {
        // Remove items outside onBindViewHolder and notify adapter properly
        positionsToRemove.forEach { position ->
            chatHistoryList.removeAt(position)
            notifyItemRemoved(position)
        }
        positionsToRemove.clear() // Clear the list after removal
    }

    inner class ChatHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)

        private fun getChatCreationTime(userId: String, chatId: String, callback: (String?) -> Unit) {
            val chatRef = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).getReference("chatbot")
                .child(userId)
                .child(chatId)

            chatRef.child("created_at").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val timestamp = snapshot.getValue(Long::class.java)
                        val formattedTime = timestamp?.let {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            sdf.format(Date(it))
                        }
                        callback(formattedTime)
                    } else {
                        callback(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error fetching data", error.toException())
                    callback(null)
                }
            })
        }

        fun bind(chatHistory: ChatHistory) {
            val chatId = chatHistory.chatId
            val userId = chatHistory.userId
            tvItemName.text = "Fetching chat creation time..."

            getChatCreationTime(userId, chatId) { createdAt ->
                Log.d("Detail", "Created at: $createdAt")
                tvItemName.text = createdAt ?: "No creation time available"
            }

            itemView.setOnClickListener {
                onItemClick(chatHistory)
            }
        }
    }

    // Function to check if there are messages in the chat
    private fun checkMessagesExistence(userId: String, chatId: String, callback: (Boolean) -> Unit) {
        val chatRef = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).getReference("chatbot")
            .child(userId)
            .child(chatId)

        chatRef.child("messages").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Pastikan chat memiliki pesan sebelum menampilkannya
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    callback(true)
                } else {
                    // Menganggap chat kosong jika tidak ada pesan
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error checking messages", error.toException())
                callback(false) // Anggap tidak ada pesan jika terjadi error
            }
        })
    }



    // Remove chat from Firebase if there are no messages
    private fun removeChatFromFirebase(userId: String, chatId: String) {
        val chatRef = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).getReference("chatbot")
            .child(userId)
            .child(chatId)

        chatRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Chat with no messages removed successfully from Firebase.")
            } else {
                Log.e("Firebase", "Failed to remove chat from Firebase.", task.exception)
            }
        }
    }
}



