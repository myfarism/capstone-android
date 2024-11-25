package com.example.capstonebangkitpawers.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.main.MainViewModel
import com.example.capstonebangkitpawers.main.ViewModelFactory
import com.example.capstonebangkitpawers.view.DataDiriActivity
import com.example.capstonebangkitpawers.view.HistoryActivity
import com.example.capstonebangkitpawers.view.SettingsActivity
import com.example.capstonebangkitpawers.view.WelcomeActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory.getInstance(requireContext())
        )[MainViewModel::class.java]

        auth = Firebase.auth

        database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).reference

        setupUser(view)
        setupLogout(view)
        setupPengaturan(view)
        setupRiwayat(view)

        return view
    }

    private fun setupUser(view: View) {
        val yourNameTextView: TextView = view.findViewById(R.id.yourName)
        val profileImageView: CircleImageView = view.findViewById(R.id.profileImage1)
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userRef = database.child("users").child(currentUser.uid)

            userRef.get()
                .addOnSuccessListener { snapshot ->
                    val name = snapshot.child("name").getValue(String::class.java) ?: "Nama tidak ditemukan"
                    val photoUrl = snapshot.child("photoUrl").getValue(String::class.java) ?: "default"

                    // Set nama pengguna
                    yourNameTextView.text = name
                    Log.d("ProfileFragment", "Nama pengguna: $name")

                    // Set foto profil
                    if (photoUrl == "default") {
                        profileImageView.setImageResource(R.drawable.profile_placeholder)
                    } else {
                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.profile_placeholder)
                            .error(R.drawable.profile_placeholder)
                            .into(profileImageView)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileFragment", "Gagal mengambil data pengguna: ${e.message}")
                    yourNameTextView.text = "Gagal memuat nama"
                    profileImageView.setImageResource(R.drawable.profile_placeholder)
                }
        } else {
            yourNameTextView.text = "Tidak ada pengguna"
            profileImageView.setImageResource(R.drawable.profile_placeholder)
        }
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

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Apakah Anda ingin logout?")
            .setPositiveButton("Ya") { dialog, _ ->
                logout()
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun logout() {
        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(requireContext())
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val database = FirebaseDatabase.getInstance(databaseURL)
                val userRef = database.getReference("users").child(currentUser.uid)

                try {
                    // Perbarui isLogin menjadi false di Realtime Database
                    userRef.child("isLogin").setValue(false).await()
                    Log.d("Logout", "isLogin diperbarui menjadi false.")
                } catch (e: Exception) {
                    Log.e("Logout", "Gagal memperbarui isLogin: ${e.message}")
                }

                // Lanjutkan proses logout
                performLogout(credentialManager)
            } else {
                // Jika pengguna tidak ditemukan, langsung logout
                performLogout(credentialManager)
            }
        }
    }

    private suspend fun performLogout(credentialManager: CredentialManager) {
        withContext(Dispatchers.IO) {
            try {
                auth.signOut()
                credentialManager.clearCredentialState(ClearCredentialStateRequest())

                withContext(Dispatchers.Main) {
                    // Navigasi ke WelcomeActivity di Main Thread
                    val intent = Intent(requireContext(), WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    Log.d("Logout", "Proses logout selesai. Pengguna diarahkan ke WelcomeActivity.")
                }
            } catch (e: Exception) {
                Log.e("Logout", "Terjadi kesalahan saat logout: ${e.message}")
            }
        }
    }

    companion object {
        const val databaseURL = BuildConfig.DATABASE_URL
    }
}
