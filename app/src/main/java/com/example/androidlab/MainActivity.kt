package com.example.androidlab

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                Log.d("kkang", "Touch down event x: ${event.x}, rawX: ${event.rawX}")
            }
            MotionEvent.ACTION_UP->{
                Log.d("kkang", "Touch up event")
            }
        }
        return super.onTouchEvent(event)
    }
}