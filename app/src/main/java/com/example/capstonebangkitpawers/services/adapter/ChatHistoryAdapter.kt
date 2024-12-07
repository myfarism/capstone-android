package com.example.capstonebangkitpawers.services.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.services.ChatHistory

class ChatHistoryAdapter(
    private val chatHistoryList: List<ChatHistory>,
    private val onItemClick: (ChatHistory) -> Unit
) : RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chatbot, parent, false)
        return ChatHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHistoryViewHolder, position: Int) {
        val chatHistory = chatHistoryList[position]
        holder.bind(chatHistory)
    }

    override fun getItemCount(): Int = chatHistoryList.size

    inner class ChatHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView5)

        fun bind(chatHistory: ChatHistory) {
            // Mengatur nama percakapan
            //tvItemName.text = "Chat History"

            // Bisa menambahkan lebih banyak logika untuk gambar jika diperlukan
            //imageView.setImageResource(R.drawable.logopawersfull)  // Gambar tetap menggunakan logo Pawers

            // Menangani klik pada item
            itemView.setOnClickListener {
                onItemClick(chatHistory)
            }
        }
    }
}
