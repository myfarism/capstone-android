package com.example.capstonebangkitpawers.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.capstonebangkitpawers.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _profileImageUrl = MutableLiveData<String>()
    val profileImageUrl: LiveData<String> get() = _profileImageUrl

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL)

    fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val userRef = database.getReference("users").child(it)
            userRef.get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java) ?: "Nama tidak ditemukan"
                val photoUrl = snapshot.child("photoUrl").getValue(String::class.java) ?: "default"
                _userName.postValue(name)
                _profileImageUrl.postValue(photoUrl)
            }
        }
    }

    fun updateName(userId: String, name: String) {
        val userRef = database.getReference("users").child(userId)
        userRef.child("name").setValue(name).addOnCompleteListener {
            if (it.isSuccessful) {
                _userName.postValue(name)
            }
        }
    }

    fun updateProfileImage(userId: String, imagePath: String) {
        val userRef = database.getReference("users").child(userId)
        userRef.child("photoUrl").setValue(imagePath).addOnCompleteListener {
            if (it.isSuccessful) {
                _profileImageUrl.postValue(imagePath)
            }
        }
    }
}
