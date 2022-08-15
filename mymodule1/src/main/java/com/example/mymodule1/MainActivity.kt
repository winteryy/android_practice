package com.example.mymodule1

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mymodule1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    var initTime = 0L
    var pauseTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startButton.setOnClickListener {
            binding.chronometer.base = SystemClock.elapsedRealtime() - pauseTime
            binding.chronometer.start()

            binding.startButton.isEnabled = false
            binding.stopButton.isEnabled = true
            binding.resetButton.isEnabled = true
        }
        binding.stopButton.setOnClickListener {
            pauseTime = SystemClock.elapsedRealtime() - binding.chronometer.base
            binding.chronometer.stop()
            Log.d("확인용", "${pauseTime}")

            binding.startButton.isEnabled = true
            binding.stopButton.isEnabled = false
            binding.resetButton.isEnabled = true
        }
        binding.resetButton.setOnClickListener {
            binding.chronometer.base = SystemClock.elapsedRealtime()
            binding.chronometer.stop()
            pauseTime = 0L

            binding.startButton.isEnabled = true
            binding.stopButton.isEnabled = true
            binding.resetButton.isEnabled = false
        }
    }

    override fun onBackPressed() {
        if(System.currentTimeMillis() - initTime > 3000){
            Toast.makeText(this, "종료하려면 한 번 더 누르세요",
                Toast.LENGTH_SHORT).show()
            initTime = System.currentTimeMillis()
        }else {
            super.onBackPressed()
        }
    }
}