package com.example.capstonebangkitpawers.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.databinding.ActivityOutputBinding
import com.example.capstonebangkitpawers.main.MainActivity
import com.example.capstonebangkitpawers.services.CheckPenyakit
import com.google.firebase.database.FirebaseDatabase

class OutputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOutputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getParcelableExtra<Uri>("imageUri")
        val predictedLabel = intent.getStringExtra("predictedLabel")

        binding.txtHasil.text = predictedLabel
        binding.capturedImageView.setImageURI(imageUri)

        if (predictedLabel != null) {
            checkPenyakit(predictedLabel)
        }

        binding.buttonBackhome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        binding.buttonBackscan.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    private fun checkPenyakit(label: String) {
        val database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL)
        val penyakitRef = database.reference.child("penyakit")

        val checkPenyakit = CheckPenyakit()
        checkPenyakit.checkPenyakit(penyakitRef, label) { penyakit ->
            if (penyakit != null) {
                binding.txtPenjelasan.text = penyakit.deskripsi
                binding.txtPencegahan.text = penyakit.pencegahan
            } else {
                binding.txtPenjelasan.text = "Deskripsi tidak ditemukan."
                binding.txtPencegahan.text = "Pencegahan tidak ditemukan."
            }
        }

    }
}
