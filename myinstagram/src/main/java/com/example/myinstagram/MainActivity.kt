package com.example.myinstagram

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myinstagram.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        binding.theText.text = "Hello, " + "${MyApplication.email}"

        //네비게이션 바 아이콘 틴트 효과 제거
        binding.bottomNav.itemIconTintList=null

    }
}