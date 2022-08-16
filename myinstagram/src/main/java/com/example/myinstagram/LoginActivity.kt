package com.example.myinstagram

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myinstagram.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        //텍스트 색 처리
        val ssb = SpannableStringBuilder(binding.registerBtn.text)
        ssb.setSpan(ForegroundColorSpan(resources.getColor(R.color.lapis)),
        11, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.registerBtn.text = ssb

        //로그인 구현부
        var idCheck: Int = 0
        var pwCheck: Int = 0

        binding.userId.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(editable: Editable) {
                idCheck = editable.length
                binding.loginBtn.isEnabled = idCheck>0 && pwCheck>0
            }
        })
        binding.userPw.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(editable: Editable) {
                pwCheck = editable.length
                binding.loginBtn.isEnabled = idCheck>0 && pwCheck>0
            }
        })

        binding.loginBtn.setOnClickListener {
            val userId: String = binding.userId.text.toString()
            val userPw: String = binding.userPw.text.toString()
            Log.d("ID", userId)
            Log.d("PW", userPw)
        }

        //회원가입
        binding.registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

}