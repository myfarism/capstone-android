package com.example.capstonebangkitpawers.view

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.databinding.ActivityHistoryBinding
import com.example.capstonebangkitpawers.services.Riwayat
import com.example.capstonebangkitpawers.services.adapter.HistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyAdapter = HistoryAdapter()
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        loadHistoryData()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadHistoryData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

        val database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL)
        val riwayatRef = database.reference.child("riwayat").child(userId)

        riwayatRef.orderByChild("date").limitToLast(10).get().addOnSuccessListener { snapshot ->
            val historyList = mutableListOf<Riwayat>()
            snapshot.children.forEach { data ->
                val riwayat = data.getValue(Riwayat::class.java)
                riwayat?.let {
                    historyList.add(it)
                }
            }

            historyAdapter.submitList(historyList)
        }.addOnFailureListener { exception ->
            Log.e("HistoryActivity", "Failed to load history: ${exception.message}", exception)
            Toast.makeText(this, "Failed to load history. Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
}



