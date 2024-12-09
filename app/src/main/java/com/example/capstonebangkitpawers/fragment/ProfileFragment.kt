package com.example.capstonebangkitpawers.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.login.LoginActivity.Companion.databaseURL
import com.example.capstonebangkitpawers.main.MainViewModel
import com.example.capstonebangkitpawers.view.DataDiriActivity
import com.example.capstonebangkitpawers.view.HistoryActivity
import com.example.capstonebangkitpawers.view.SettingsActivity
import com.example.capstonebangkitpawers.view.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).reference

        mainViewModel.userName.observe(viewLifecycleOwner, Observer { name ->
            val yourNameTextView: TextView = view.findViewById(R.id.yourName)
            yourNameTextView.text = name
        })

        mainViewModel.profileImageUrl.observe(viewLifecycleOwner, Observer { photoUrl ->
            val profileImageView: CircleImageView = view.findViewById(R.id.profileImage1)
            if (photoUrl == "default") {
                profileImageView.setImageResource(R.drawable.profile_placeholder)
            } else {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .into(profileImageView)
            }
        })

        mainViewModel.loadUserData()

        setupLogout(view)
        setupPengaturan(view)
        setupRiwayat(view)
        setupDataDiri(view)

        return view
    }

    private fun setupLogout(view: View) {
        val logoutContainer: View = view.findViewById(R.id.logoutContainer)
        logoutContainer.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupPengaturan(view: View) {
        val pengaturan: View = view.findViewById(R.id.pengaturanContainer)
        pengaturan.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    private fun setupRiwayat(view: View) {
        val riwayat: View = view.findViewById(R.id.riwayatContainer)
        riwayat.setOnClickListener {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    private fun setupDataDiri(view: View) {
        val dataDiri: View = view.findViewById(R.id.dataDiriContainer)
        dataDiri.setOnClickListener {
            val intent = Intent(requireContext(), DataDiriActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Apakah Anda ingin logout?")
            .setPositiveButton("Ya") { dialog, _ ->
                updateLogoutStatus()
                logout()
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun updateLogoutStatus() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance(databaseURL)
            val userRef = database.getReference("users").child(userId)

            userRef.child("isLogin").setValue(false)
                .addOnSuccessListener {
                    Log.d("LoginActivity", "Status login pengguna diperbarui: false")
                }
                .addOnFailureListener { e ->
                    Log.e("LoginActivity", "Gagal memperbarui status login: ${e.message}")
                }
        } else {
            Log.e("LoginActivity", "Pengguna belum terautentikasi, tidak bisa memperbarui status login.")
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.loadUserData()
    }
}

