package com.example.capstonebangkitpawers.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.view.ChatViewActivity
import com.example.capstonebangkitpawers.view.HistoryActivity
import com.example.capstonebangkitpawers.view.ScanActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BerandaFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        val pindai: LinearLayout = view.findViewById(R.id.pindai)

        val riwayat: LinearLayout = view.findViewById(R.id.riwayat)

        pindai.setOnClickListener {
            val intent = Intent(activity, ScanActivity::class.java)
            startActivity(intent)
        }

        riwayat.setOnClickListener {
            val intent = Intent(activity, HistoryActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}