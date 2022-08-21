package com.example.myinstagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.example.myinstagram.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        binding.theText.text = "Hello, " + "${MyApplication.email}"

        //네비게이션 바 아이콘 틴트 효과 제거
        binding.bottomNav.itemIconTintList=null

        //메인 콘텐트 부분 프래그먼트 지정
        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().add(R.id.main_content,
        HomeFragment()).commit()

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.action_home -> {
                    fragmentManager.beginTransaction().replace(
                        R.id.main_content,HomeFragment()).commit()
                    true
                }
                R.id.action_search -> {
                    fragmentManager.beginTransaction().replace(
                        R.id.main_content ,SearchFragment()).commit()
                    true
                }
                R.id.action_upload -> {
                    if(ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED){
                        startActivity(Intent(this, AddPhotoActivity::class.java))
                    }else{
                        if(ActivityCompat.shouldShowRequestPermissionRationale(
                                this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        ){
                        }else {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 99
                            )
                        }
                    }
                    true
                }
                R.id.action_favorite -> {
                    fragmentManager.beginTransaction().replace(
                        R.id.main_content, ActiveFragment()).commit()
                    true
                }
                R.id.action_account -> {
                    fragmentManager.beginTransaction().replace(
                        R.id.main_content, AccountFragment()).commit()
                    true
                }
                else -> false
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            99 -> {
                if (grantResults.size > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startActivity(Intent(this, AddPhotoActivity::class.java))
                    } else {
                        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)){
                            AlertDialog.Builder(this).setTitle("권한 요청")
                                .setMessage("업로드 기능을 이용하려면 저장소 권한이 필요합니다")
                                .setPositiveButton("확인"){
                                        dialog, which -> ActivityCompat.requestPermissions(this@MainActivity,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 99)
                                }
                                .setNegativeButton("취소", null)
                                .create().show()
                        }
                        else {
                            Log.d("MainActivity", "권한 거부로 종료")
                        }
                    }
                }
            }
        }

    }
    private fun showPermissionInfoDialog(){

    }
}