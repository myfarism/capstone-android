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
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.databinding.ActivityLoginBinding
import com.example.capstonebangkitpawers.main.MainActivity
import com.example.capstonebangkitpawers.main.ViewModelFactory
import com.example.capstonebangkitpawers.register.RegisterActivity
import com.example.capstonebangkitpawers.user.UserModel

class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rawString = "Tidak punya akun? Buat Akun"
        val spannableString = SpannableString(rawString.trim())

        Log.d("StringContent", "Spannable String: '$spannableString' with length: ${spannableString.length}")

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }

        spannableString.setSpan(clickableSpan, 17, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(getColor(android.R.color.holo_blue_light)), 17, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvSandiMasuk.text = spannableString
        binding.tvSandiMasuk.movementMethod = LinkMovementMethod.getInstance()
        binding.tvSandiMasuk.text = spannableString
        binding.tvSandiMasuk.movementMethod = LinkMovementMethod.getInstance()

        addTextWatcher()
        setupAction()
    }

    private fun addTextWatcher() {
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty() || s.length < 8) {
                    binding.passwordEditTextLayout.error = "Password tidak boleh kurang dari 8 karakter"
                } else {
                    binding.passwordEditTextLayout.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupAction() {
        binding.btnMasuk.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (password.length < 8) {
                AlertDialog.Builder(this).apply {
                    setTitle("Error")
                    setMessage("Password harus terdiri dari minimal 8 karakter.")
                    setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    create()
                    show()
                }
            } else {
                viewModel.saveSession(UserModel(email, "sample_token"))
                AlertDialog.Builder(this).apply {
                    setTitle("Yeah!")
                    setMessage("Anda berhasil login!")
                    setPositiveButton("Lanjut") { _, _ ->
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                    create()
                    show()
                }
            }
        }
    }
}
