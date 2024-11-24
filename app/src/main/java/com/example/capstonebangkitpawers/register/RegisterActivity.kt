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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.databinding.ActivityRegisterBinding
import com.example.capstonebangkitpawers.login.LoginActivity
import com.example.capstonebangkitpawers.main.MainActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickableText()
        setupFirebaseAuth()
        setupListeners()
    }

    private fun setupFirebaseAuth() {
        auth = FirebaseAuth.getInstance()
    }

    private fun setupClickableText() {
        val spannableString = SpannableString("Sudah punya akun? Masuk")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
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

    private fun setupListeners() {
        binding.btnGoogle.setOnClickListener { regisrerGoogle() }
        binding.btnDaftar.setOnClickListener { registerEmail() }
        addTextWatchers()
    }

    private fun registerEmail() {
        val email = binding.emailEditText.text.toString().trim()
        val name = binding.userEditText.text.toString().trim()
        val password = binding.pwEditText.text.toString().trim()
        val confirmPassword = binding.confirmpwEditText.text.toString().trim()

        if (!validateInput(email, name, password, confirmPassword)) return

        binding.progressBar.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.uid?.let { saveUserDataToDatabase(it, name, email) }
                    showAlertDialog("Pendaftaran Berhasil", "Akun Anda berhasil dibuat.") {
                        navigateToLogin()
                    }
                } else {
                    showAlertDialog("Pendaftaran Gagal", task.exception?.message ?: "Terjadi kesalahan.")
                }
            }
    }

    private fun saveUserDataToDatabase(uid: String, name: String, email: String) {
        val userData = mapOf("name" to name, "email" to email)
        FirebaseDatabase.getInstance().getReference("users").child(uid)
            .setValue(userData)
            .addOnSuccessListener { Log.d(TAG, "Data pengguna berhasil disimpan.") }
            .addOnFailureListener { e -> Log.e(TAG, "Gagal menyimpan data: ${e.message}") }
    }

    private fun validateInput(email: String, name: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            showAlertDialog("Error", "Nama tidak boleh kosong.")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showAlertDialog("Error", "Format email tidak valid.")
            return false
        }
        if (password.length < 8) {
            binding.pwEditTextLayout.error = "Password harus terdiri dari minimal 8 karakter."
            return false
        }
        if (password != confirmPassword) {
            binding.confirmpwEditTextLayout.error = "Konfirmasi password tidak cocok."
            return false
        }
        return true
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
            .show()
    }

    private fun regisrerGoogle() {
        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = this@RegisterActivity,
                )
            } catch (e: GetCredentialException) {
                Log.d("Error", e.message.toString())
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun addTextWatchers() {
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
        val password = binding.pwEditText.text.toString()
        val confirmPassword = binding.confirmpwEditText.text.toString()

        binding.pwEditTextLayout.error = if (password.length < 8) "Password minimal 8 karakter" else null
        binding.confirmpwEditTextLayout.error = if (password != confirmPassword) "Password tidak cocok" else null
    }

    companion object {
        private const val TAG = "RegisterActivity"
        const val webClientId = BuildConfig.WEB_CLIENT_ID
    }
}
