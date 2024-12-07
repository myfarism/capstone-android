package com.example.capstonebangkitpawers.services

data class Message(
    val senderName: String,
    val content: String,
    val timestamp: String,
    val isUserMessage: Boolean
)
