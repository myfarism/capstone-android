package com.example.capstonebangkitpawers.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.databinding.ActivityDataDiriBinding
import com.example.capstonebangkitpawers.fragment.ProfileFragment
import com.example.capstonebangkitpawers.main.MainViewModel
import com.example.capstonebangkitpawers.main.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DataDiriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataDiriBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var mainViewModel: MainViewModel
    private var imageUri: Uri? = null

    private val getImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.profileImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataDiriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL)

        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory.getInstance(application)
        ).get(MainViewModel::class.java)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        loadUserData()

        binding.profileImageContainer.setOnClickListener {
            openGalleryForImage()
        }

        binding.btnSimpan.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            if (name.isNotEmpty()) {
                updateName(name)
            }

            if (imageUri != null) {
                saveProfileImageLocally(imageUri!!)
            } else if (name.isNotEmpty()) {
                goToHome()
            }
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        user?.let {
            val userRef = database.getReference("users").child(it.uid)
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val profileImagePath = snapshot.child("photoUrl").getValue(String::class.java)
                    if (!profileImagePath.isNullOrEmpty()) {
                        val imageFile = File(profileImagePath)
                        if (imageFile.exists()) {
                            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                            binding.profileImage.setImageBitmap(bitmap)
                        }
                    }
                }
            }
        }
    }

    private fun updateName(name: String) {
        val user = auth.currentUser
        user?.let {
            val uid = it.uid
            val userRef = database.getReference("users").child(it.uid)
            userRef.child("name").setValue(name).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mainViewModel.updateName(uid, name)
                    Toast.makeText(this, "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Toast.makeText(this, "Gagal memperbarui nama", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openGalleryForImage() {
        getImageFromGallery.launch("image/*")
    }

    private fun saveProfileImageLocally(uri: Uri) {
        val user = auth.currentUser
        user?.let {
            val uid = it.uid
            val fileName = "${it.uid}_profile_image_${UUID.randomUUID()}.jpg"
            val file = File(filesDir, fileName)

            try {
                val inputStream = contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)

                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val userRef = database.getReference("users").child(it.uid)
                userRef.child("photoUrl").setValue(file.absolutePath).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mainViewModel.updateProfileImage(uid, file.absolutePath)
                        Toast.makeText(this, "Foto profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        binding.profileImage.setImageBitmap(bitmap)
                        goToHome()
                    } else {
                        Toast.makeText(this, "Gagal memperbarui foto profil", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Gagal menyimpan gambar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun goToHome() {
        val intent = Intent(this, ProfileFragment::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
