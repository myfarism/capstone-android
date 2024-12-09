package com.example.capstonebangkitpawers.services.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.services.Message

class MessageAdapter(private val messageList: MutableList<Message>) : RecyclerView.Adapter<MessageAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderName: TextView = itemView.findViewById(R.id.senderName)
        val messageContent: TextView = itemView.findViewById(R.id.messageContent)
        val messageTimestamp: TextView = itemView.findViewById(R.id.messageTimestamp)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ChatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messageList[position]

        holder.messageContent.text = message.content
        holder.messageTimestamp.text = message.timestamp

        if (message.isUserMessage) {
            holder.senderName.text = message.senderName
            holder.itemView.layoutParams = (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = 120
                marginEnd = 10
            }
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.green))

        } else {
            holder.senderName.text = message.senderName
            holder.itemView.layoutParams = (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = 10
                marginEnd = 120
            }
        }
    }


    override fun getItemCount(): Int = messageList.size
}

