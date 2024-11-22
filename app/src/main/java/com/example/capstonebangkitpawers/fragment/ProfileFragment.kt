package com.example.capstonebangkitpawers.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory.getInstance(requireContext())
        )[MainViewModel::class.java]

        val logoutContainer: View = view.findViewById(R.id.logoutContainer)
        logoutContainer.setOnClickListener {
            showLogoutDialog()
        }

        auth = Firebase.auth
        val firebaseUser = auth.currentUser



        val pengaturan: View = view.findViewById(R.id.pengaturanContainer)
        pengaturan.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }



        val riwayat: View = view.findViewById(R.id.riwayatContainer)
        riwayat.setOnClickListener {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        return view
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
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())

            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }


//    private fun logout() {
//        mainViewModel.logout()
//        val intent = Intent(requireContext(), WelcomeActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//    }
}
