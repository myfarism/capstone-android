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
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.databinding.ActivityRegisterBinding
import com.example.capstonebangkitpawers.login.LoginActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val spannableString = SpannableString("Sudah punya akun? Masuk")
        Log.d("RegisterActivity", "Spannable String: '$spannableString' with length: ${spannableString.length}")

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        spannableString.setSpan(clickableSpan, 18, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(getColor(android.R.color.holo_blue_light)), 18, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvMasuk.text = spannableString
        binding.tvMasuk.movementMethod = LinkMovementMethod.getInstance()

        addTextWatchers()
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

        if (password.length < 8) {
            binding.pwEditTextLayout.error = "Password must be at least 8 characters"
        } else {
            binding.pwEditTextLayout.error = null
        }

        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
            binding.confirmpwEditTextLayout.error = "Passwords do not match"
        } else {
            binding.confirmpwEditTextLayout.error = null
        }
    }
}
