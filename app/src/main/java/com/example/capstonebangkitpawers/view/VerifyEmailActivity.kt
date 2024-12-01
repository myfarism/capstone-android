package com.example.capstonebangkitpawers.view

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.databinding.ActivityVerifyEmailBinding
import com.example.capstonebangkitpawers.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    private lateinit var auth: FirebaseAuth
    private var email: String? = null
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        email = intent.getStringExtra("userEmail")

        //binding.textView3.text = "Kami telah mengirimkan link verifikasi ke\n$email. Silakan periksa inbox Anda."

        setupListeners()
        startResendButtonTimer()
    }

    private fun setupListeners() {
        binding.btnVerify.setOnClickListener { checkVerificationStatus() }
        binding.btnResend.setOnClickListener { resendVerificationEmail() }
    }

    private fun checkVerificationStatus() {
        val user = auth.currentUser

        binding.progressBar.visibility = View.VISIBLE

        user?.reload()?.addOnCompleteListener { task ->
            binding.progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                if (user.isEmailVerified) {
                    updateUserVerificationStatus(user.uid)
                    navigateToLogin()
                } else {
                    showAlertDialog("Belum Diverifikasi", "Email Anda belum terverifikasi. Silakan cek kembali.")
                }
            } else {
                showAlertDialog("Error", "Gagal memeriksa status verifikasi.")
            }
        }
    }

    private fun updateUserVerificationStatus(uid: String) {
        val database = FirebaseDatabase.getInstance(databaseURL)
        val userRef = database.getReference("users").child(uid)

        userRef.child("isVerified").setValue(true)
            .addOnSuccessListener {
                Log.d("VerifyEmailActivity", "Status verifikasi pengguna diperbarui di database.")
            }
            .addOnFailureListener { e ->
                Log.e("VerifyEmailActivity", "Gagal memperbarui status verifikasi: ${e.message}")
            }
    }

    private fun resendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showAlertDialog("Berhasil", "Email verifikasi telah dikirim ulang.")
                startResendButtonTimer() // Reset timer setelah pengiriman ulang
            } else {
                showAlertDialog("Error", "Gagal mengirim ulang email verifikasi.")
            }
        }
    }

    private fun navigateToLogin() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun startResendButtonTimer() {
        // Nonaktifkan tombol kirim ulang dan tampilkan timer
        binding.btnResend.isEnabled = false
        binding.timerTextView.visibility = View.VISIBLE

        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                binding.timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.btnResend.isEnabled = true
                binding.timerTextView.visibility = View.GONE
            }
        }
        timer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }


    companion object {
        const val databaseURL = BuildConfig.DATABASE_URL
    }
}
