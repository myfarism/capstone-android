package com.example.capstonebangkitpawers.user

data class UserModel(
    val email: String,
    val token: String,
    val isLogin: Boolean = false
)