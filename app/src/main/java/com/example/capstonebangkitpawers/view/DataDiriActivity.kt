package com.example.capstonebangkitpawers.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonebangkitpawers.databinding.ActivityDataDiriBinding

class DataDiriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataDiriBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataDiriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }


    }
}