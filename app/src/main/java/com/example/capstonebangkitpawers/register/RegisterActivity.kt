package com.example.capstonebangkitpawers.register

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.databinding.ActivityRegisterBinding
import com.example.capstonebangkitpawers.login.LoginActivity
import com.example.capstonebangkitpawers.main.MainActivity
import com.example.capstonebangkitpawers.view.VerifyEmailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addTextWatchers()
        setupFirebaseAuth()
        setupListeners()
        setupClickableText()
    }

    private fun setupFirebaseAuth() {
        auth = FirebaseAuth.getInstance()
    }

    private fun setupListeners() {
        binding.btnDaftar.setOnClickListener { registerEmail() }
    }

    private fun registerEmail() {
        val email = binding.emailEditText.text.toString().trim()
        val name = binding.userEditText.text.toString().trim()
        val password = binding.pwEditText.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showAlertDialog("Error", "Format email tidak valid.")
        }

        validatePasswords()

        binding.progressBar.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserToDatabase(user.uid, name, email)
                        sendVerificationEmail(user)
                    }
                } else {
                    showAlertDialog("Pendaftaran Gagal", task.exception?.message ?: "Terjadi kesalahan.")
                }
            }
    }

    private fun saveUserToDatabase(uid: String, name: String, email: String) {
        val database = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL)
        val userRef = database.getReference("users").child(uid)

        val userData = mapOf(
            "name" to name,
            "email" to email,
            "isVerified" to false,
            "photoUrl" to "default",
            "isLogin" to false
        )

        userRef.setValue(userData)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Data pengguna berhasil disimpan: $userData")
            }
            .addOnFailureListener { e ->
                Log.e("RegisterActivity", "Gagal menyimpan data pengguna: ${e.message}")
            }
    }

    private fun sendVerificationEmail(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navigateToVerifyEmail(user.email ?: "")
                } else {
                    showAlertDialog("Error", "Gagal mengirim email verifikasi.")
                }
            }
    }

    private fun navigateToVerifyEmail(email: String) {
        val intent = Intent(this, VerifyEmailActivity::class.java)
        intent.putExtra("userEmail", email)
        startActivity(intent)
        finish()
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupClickableText() {
        val spannableString = SpannableString("Sudah punya akun? Masuk")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                navigateToLogin()
            }
        }
        spannableString.setSpan(clickableSpan, 18, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            ForegroundColorSpan(getColor(android.R.color.holo_blue_light)),
            18, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvMasuk.text = spannableString
        binding.tvMasuk.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun addTextWatchers() {
        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                    binding.emailEditTextLayout.error = "Format email tidak valid"
                } else {
                    binding.emailEditTextLayout.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.userEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    binding.userEditTextLayout.error = "Nama tidak boleh kosong"
                } else {
                    binding.userEditTextLayout.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.pwEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePasswords()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.confirmpwEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePasswords()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validatePasswords() {
        val password = binding.pwEditText.text.toString().trim()
        val confirmPassword = binding.confirmpwEditText.text.toString().trim()

        if (password.length < 8) {
            binding.pwEditTextLayout.error = "Kata sandi harus terdiri dari setidaknya 8 karakter."
        } else {
            binding.pwEditTextLayout.error = null
        }

        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
            binding.confirmpwEditTextLayout.error = "Passwords tidak sama"
        } else {
            binding.confirmpwEditTextLayout.error = null
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

