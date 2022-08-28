package com.example.myinstagram

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myinstagram.MyApplication.Companion.auth
import com.example.myinstagram.databinding.ActivityUserNameBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserNameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var fireStore: FirebaseFirestore? = FirebaseFirestore.getInstance()
        var uid: String? = FirebaseAuth.getInstance().currentUser?.uid
        var userName: String? = null
        val binding = ActivityUserNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var lenCheck: Int

        binding.userNameText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(editable: Editable) {
                lenCheck = editable.length
                binding.userNameRegisterBtn.isEnabled = (lenCheck>=2)
            }
        })

        binding.userNameRegisterBtn.setOnClickListener {
            userName = binding.userNameText.text.toString()
            val nameArray: ArrayList<String> = arrayListOf()

            fireStore?.collection("userInfo")?.get()?.addOnSuccessListener {
                for(userInfo in it){
                    nameArray.add(userInfo?.get("userName").toString())
                }
                if(nameArray.isEmpty() || userName!! !in nameArray){
                    fireStore?.collection("userInfo")?.document(uid!!)?.set(hashMapOf(
                        "userName" to userName,
                        "uid" to uid
                    ))
                    Toast.makeText(this, "계정명 설정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }else{
                    Toast.makeText(this, "중복된 계정명입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.signOutText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            auth?.signOut()
        }
    }
}