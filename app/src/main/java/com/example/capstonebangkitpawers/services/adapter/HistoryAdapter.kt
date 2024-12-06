package com.example.capstonebangkitpawers.services.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.services.Riwayat

class HistoryAdapter : ListAdapter<Riwayat, HistoryAdapter.ViewHolder>(Riwayat.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val riwayat = getItem(position)

        holder.dateTextView.text = riwayat.date
        holder.predictedLabelTextView.text = riwayat.predictedLabel

        // Ensure that we check for the correct URI type and handle accordingly
        val imageUri = riwayat.imageUri
        if (imageUri != null) {
            // For content URIs, use content resolver with proper permission handling
            try {
                Glide.with(holder.itemView.context)
                    .load(imageUri)
                    .into(holder.imageView)
            } catch (e: SecurityException) {
                // Handle permission issues or fallback
                Toast.makeText(holder.itemView.context, "Permission issue loading image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.historyDate)
        val predictedLabelTextView: TextView = itemView.findViewById(R.id.historyLabel)
        val imageView: ImageView = itemView.findViewById(R.id.historyImage)
    }
}
