package com.example.capstonebangkitpawers.services

import androidx.recyclerview.widget.DiffUtil


data class Riwayat(
    val userId: String = "",
    val imageUri: String = "",
    val predictedLabel: String = "",
    val date: String = ""
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Riwayat>() {
            override fun areItemsTheSame(oldItem: Riwayat, newItem: Riwayat): Boolean {
                return oldItem.userId == newItem.userId && oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: Riwayat, newItem: Riwayat): Boolean {
                return oldItem == newItem
            }
        }
    }
}



