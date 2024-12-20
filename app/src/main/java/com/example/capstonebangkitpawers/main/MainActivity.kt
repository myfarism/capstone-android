package com.example.capstonebangkitpawers.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.databinding.ActivityMainBinding
import com.example.capstonebangkitpawers.fragment.BerandaFragment
import com.example.capstonebangkitpawers.fragment.ChatBotFragment
import com.example.capstonebangkitpawers.fragment.ProfileFragment
import com.example.capstonebangkitpawers.view.ScanActivity
import com.example.capstonebangkitpawers.view.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: Toolbar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = binding.bottomNavigation

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }

        val scanIcon: ImageView = findViewById(R.id.captureImgBtn)
        scanIcon.setOnClickListener {
            if (checkAndRequestPermissions()) {
                navigateToScanActivity()
            }
        }

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToWelcomeActivity()
        } else {
            checkLoginStatus(currentUser.uid)
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_beranda
            replaceFragment(BerandaFragment())
        }

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_beranda -> replaceFragment(BerandaFragment())
                R.id.nav_chat -> replaceFragment(ChatBotFragment())
                R.id.nav_profil -> replaceFragment(ProfileFragment())
                else -> false
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        toolbar.visibility = if (fragment is BerandaFragment) View.VISIBLE else View.GONE
        return true
    }

    private fun checkLoginStatus(uid: String) {
        val database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL)
        val userRef = database.getReference("users").child(uid)

        userRef.child("isLogin").get()
            .addOnSuccessListener { snapshot ->
                val isLogin = snapshot.getValue(Boolean::class.java) ?: false
                if (!isLogin) {
                    navigateToWelcomeActivity()
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Gagal memeriksa status login: ${e.message}")
            }
    }

    private fun navigateToWelcomeActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun navigateToScanActivity() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivity(intent)
    }

    private fun checkAndRequestPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val listPermissionsNeeded = ArrayList<String>()

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 1)
            return false
        }
        return true
    }
}
