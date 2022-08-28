package com.example.myinstagram

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myinstagram.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    var auth : FirebaseAuth ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        //글자수 체크
        var idCheck: Int = 0
        var pwCheck: Int = 0

        binding.userId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(editable: Editable) {
                idCheck = editable.length
                binding.signUpBtn.isEnabled = idCheck>0 && pwCheck>0
            }
        })
        binding.userPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(editable: Editable) {
                pwCheck = editable.length
                binding.signUpBtn.isEnabled = idCheck>0 && pwCheck>0
            }
        })

        //회원가입
        binding.signUpBtn.setOnClickListener {
            val email:String = binding.userId.text.toString()
            val password:String = binding.userPw.text.toString()

            auth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener {
                task ->
                    if(task.isSuccessful){
                        Toast.makeText(this, "가입 성공",
                            Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }else{
                        Toast.makeText(baseContext, "중복되거나 유효하지 않은 이메일/비밀번호 입력입니다.",
                            Toast.LENGTH_SHORT).show()
                    }
            }

        }
    }
}