package com.example.capstonebangkitpawers.services.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.services.ChatHistory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatHistoryAdapter(
    private val chatHistoryList: MutableList<ChatHistory>,
    private val onItemClick: (ChatHistory) -> Unit
) : RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chatbot, parent, false)
        return ChatHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHistoryViewHolder, position: Int) {
        val chatHistory = chatHistoryList[position]

        if (chatHistory.chatId.isNotEmpty()) {
            checkMessagesExistence(chatHistory.userId, chatHistory.chatId) { hasMessages ->
                if (hasMessages) {
                    holder.bind(chatHistory)
                } else {
                    removeChatFromFirebase(chatHistory.userId, chatHistory.chatId)
                    chatHistoryList.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        } else {
            holder.itemView.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int {
        return chatHistoryList.size
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

    private fun checkMessagesExistence(userId: String, chatId: String, callback: (Boolean) -> Unit) {
        val chatRef = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).getReference("chatbot")
            .child(userId)
            .child(chatId)

        chatRef.child("messages").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    callback(true)
                } else {
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error checking messages", error.toException())
                callback(false)
            }
        })
    }

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



