package com.example.capstonebangkitpawers.login

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
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.capstonebangkitpawers.BuildConfig
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.databinding.ActivityLoginBinding
import com.example.capstonebangkitpawers.main.MainActivity
import com.example.capstonebangkitpawers.main.ViewModelFactory
import com.example.capstonebangkitpawers.register.RegisterActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

//    private val viewModel by viewModels<LoginViewModel> {
//        ViewModelFactory.getInstance(this)
//    }
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupClickableText()
        addTextWatcher()
        setupAction()
    }

    private fun setupClickableText() {
        val rawString = "Tidak punya akun? Buat Akun"
        val spannableString = SpannableString(rawString.trim())

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }

        spannableString.setSpan(clickableSpan, 17, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            ForegroundColorSpan(getColor(android.R.color.holo_blue_light)),
            17, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvSandiMasuk.text = spannableString
        binding.tvSandiMasuk.movementMethod = LinkMovementMethod.getInstance()
    }

//    private fun loginGoogle() {
//        val credentialManager = CredentialManager.create(this)
//
//        val googleIdOption = GetGoogleIdOption.Builder()
//            .setFilterByAuthorizedAccounts(false)
//            .setServerClientId(webClientId)
//            .build()
//
//        val request = GetCredentialRequest.Builder()
//            .addCredentialOption(googleIdOption)
//            .build()
//
//        lifecycleScope.launch {
//            try {
//                val result: GetCredentialResponse = credentialManager.getCredential(
//                    request = request,
//                    context = this@LoginActivity,
//                )
//            } catch (e: GetCredentialException) {
//                Log.d("Error", e.message.toString())
//            }
//        }
//    }

    private fun loginEmail() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (!validateInputs(email, password)) return

        binding.progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        updateLoginStatus(user.uid, true)
                        navigateToMainActivity()
                    }
                } else {
                    handleLoginError(task.exception)
                }
            }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            showAlertDialog("Error", "Email tidak boleh kosong")
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showAlertDialog("Error", "Format email tidak valid")
            return false
        }

        return true
    }

    private fun updateLoginStatus(uid: String, isLogin: Boolean) {
        val database = FirebaseDatabase.getInstance(databaseURL)
        val userRef = database.getReference("users").child(uid)

        userRef.child("isLogin").setValue(isLogin)
            .addOnSuccessListener {
                Log.d("LoginActivity", "Status login pengguna diperbarui: $isLogin")
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Gagal memperbarui status login: ${e.message}")
            }
    }

    private fun handleLoginError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidUserException -> showAlertDialog("Login Gagal", "Email tidak terdaftar")
            is FirebaseAuthInvalidCredentialsException -> showAlertDialog("Login Gagal", "Email atau password salah")
            else -> showAlertDialog("Login Gagal", "Terjadi kesalahan: ${exception?.message}")
        }
    }

    private fun addTextWatcher() {
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.passwordEditTextLayout.error =
                    if (s.isNullOrEmpty() || s.length < 8) "Password minimal 8 karakter" else null
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupAction() {
        binding.btnMasuk.setOnClickListener { loginEmail() }
        //binding.btnGoogle.setOnClickListener { loginGoogle() }
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

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        const val TAG = "LoginActivity"
        const val webClientId = BuildConfig.WEB_CLIENT_ID
        const val databaseURL = BuildConfig.DATABASE_URL
    }
}
