package com.example.capstonebangkitpawers.services

import android.content.Context
import com.example.capstonebangkitpawers.user.UserPreference
import com.example.capstonebangkitpawers.user.UserRepository
import com.example.capstonebangkitpawers.user.dataStore

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        return UserRepository.getInstance(pref)
    }
}