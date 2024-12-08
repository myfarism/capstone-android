package com.example.capstonebangkitpawers.services

data class ChatHistory(
    val chatId: String,
    val userId: String,
    val content: String,
    val senderName: String,
    val timestamp: String
)

