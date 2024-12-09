package com.example.capstonebangkitpawers.view

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonebangkitpawers.databinding.ActivityUbahSandiBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class UbahSandiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUbahSandiBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUbahSandiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.btnUbah.setOnClickListener { gantiPassword() }
    }


    private fun gantiPassword() {
        val oldPassword = binding.passwordLamaEditText.text.toString().trim()
        val newPassword = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.passwordConfirmEditText.text.toString().trim()

        if (TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(this, "Kata sandi lama tidak boleh kosong", Toast.LENGTH_SHORT).show()
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Kata sandi baru tidak boleh kosong", Toast.LENGTH_SHORT).show()
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Konfirmasi kata sandi tidak cocok", Toast.LENGTH_SHORT).show()
        }

        changePassword(oldPassword, newPassword)
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        val user = auth.currentUser

        binding.progressBar.visibility = View.VISIBLE

        user?.let {
            val credential = EmailAuthProvider.getCredential(it.email!!, oldPassword)

            it.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        it.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(this, "Kata sandi berhasil diubah", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Gagal mengubah kata sandi", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Kata sandi lama salah", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}