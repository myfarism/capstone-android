package com.example.capstonebangkitpawers.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.databinding.ActivityDataDiriBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DataDiriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataDiriBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var imageUri: Uri? = null

    // Replaced onActivityResult with registerForActivityResult
    private val getImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.profileImage1.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataDiriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        loadUserData()

        binding.profileImage.setOnClickListener {
            openGalleryForImage()
        }

        binding.btnSimpan.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            if (name.isNotEmpty()) {
                updateName(name)
            }

            if (imageUri != null) {
                saveProfileImageLocally(imageUri!!)
            }
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        user?.let {
            val userRef = database.getReference("users").child(it.uid)
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    //binding.nameEditText.setText(name)

                    val profileImagePath = snapshot.child("photoUrl").getValue(String::class.java)
                    if (!profileImagePath.isNullOrEmpty()) {
                        val imageFile = File(profileImagePath)
                        if (imageFile.exists()) {
                            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                            binding.profileImage1.setImageBitmap(bitmap)
                        }
                    }
                }
            }
        }
    }

    private fun updateName(name: String) {
        val user = auth.currentUser
        user?.let {
            val userRef = database.getReference("users").child(it.uid)
            userRef.child("name").setValue(name)
                .addOnSuccessListener {
                    Toast.makeText(this, "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memperbarui nama: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openGalleryForImage() {
        getImageFromGallery.launch("image/*")
    }

    private fun saveProfileImageLocally(uri: Uri) {
        val user = auth.currentUser
        user?.let {
            val fileName = "${it.uid}_profile_image_${UUID.randomUUID()}.jpg"
            val file = File(filesDir, fileName)

            try {
                val inputStream = contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)

                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val userRef = database.getReference("users").child(it.uid)
                userRef.child("photoUrl").setValue(file.absolutePath)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Foto profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        Log.d("FOTO", file.toString())
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        binding.profileImage1.setImageBitmap(bitmap)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal memperbarui foto profil: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Gagal menyimpan gambar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


