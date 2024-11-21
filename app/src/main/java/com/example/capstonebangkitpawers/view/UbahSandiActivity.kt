package com.example.capstonebangkitpawers.view

import android.os.Bundle
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.databinding.ActivitySettingsBinding
import com.example.capstonebangkitpawers.databinding.ActivityUbahSandiBinding

class UbahSandiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUbahSandiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUbahSandiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }
}