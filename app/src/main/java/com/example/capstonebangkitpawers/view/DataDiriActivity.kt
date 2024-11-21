package com.example.capstonebangkitpawers.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.databinding.ActivityDataDiriBinding

class DataDiriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataDiriBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataDiriBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}