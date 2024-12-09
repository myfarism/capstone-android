package com.example.capstonebangkitpawers.view

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.databinding.ActivityForgotPasswordBinding
import com.example.capstonebangkitpawers.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.btnKirim.setOnClickListener{ resetPassword() }
    }


    private fun resetPassword() {
        val email = binding.emailEditText.text.toString().trim()

        binding.progressBar.visibility = View.VISIBLE

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showAlertDialog("Input Tidak Valid", "Harap masukkan email terlebih dahulu.")
            return
        }

        val databaseReference = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL).getReference("users")

        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressBar.visibility = View.GONE
                if (snapshot.exists()) {
                    sendPasswordResetEmail(email)
                } else {
                    showAlertDialog("Email Tidak Terdaftar", "Email tidak terdaftar dalam sistem kami.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showAlertDialog("Terjadi Kesalahan", "Terjadi kesalahan, silahkan coba lagi nanti.")
            }
        })
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showAlertDialog("Reset Password", "Email telah dikirim, silahkan cek inbox kamu.")
                    navigateToLogin()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        showAlertDialog("Email Tidak Terdaftar", "Email tidak terdaftar dalam sistem kami.")
                    } else {
                        showAlertDialog("Terjadi Kesalahan", "Terjadi kesalahan, silahkan coba lagi nanti.")
                    }
                }
            }
    }

    private fun showAlertDialog(title: String, message: String, onPositiveButtonClick: (() -> Unit)? = null) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onPositiveButtonClick?.invoke()
            }
            .create()
            .show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}