package com.example.myinstagram

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.example.myinstagram.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fireStore = FirebaseFirestore.getInstance()
        fireStore.collection("userInfo")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!).get().addOnSuccessListener {
                if(it.data?.get("userName")?:null == null){
                    startActivity(Intent(this, UserNameActivity::class.java))
                    finish()
                }else{
                    val binding = ActivityMainBinding.inflate(layoutInflater)
                    setContentView(binding.root)

                    //네비게이션 바 아이콘 틴트 효과 제거
                    binding.bottomNav.itemIconTintList=null

                    //메인 콘텐트 부분 프래그먼트 지정
                    val fragmentManager: FragmentManager = supportFragmentManager
                    fragmentManager.beginTransaction().add(R.id.main_content,
                        HomeFragment()).commit()


                    binding.bottomNav.setOnItemSelectedListener {
                        binding.barUserName.visibility = View.GONE
                        binding.barBackButton.visibility = View.GONE
                        binding.logo.visibility = View.VISIBLE

                        when(it.itemId){
                            R.id.action_home -> {
                                fragmentManager.beginTransaction().replace(
                                    R.id.main_content,HomeFragment()).commit()
                                binding.bottomNav.menu.getItem(4).isEnabled=true
                                true
                            }
                            R.id.action_search -> {
                                fragmentManager.beginTransaction().replace(
                                    R.id.main_content ,SearchFragment()).commit()
                                binding.bottomNav.menu.getItem(4).isEnabled=true
                                true
                            }
                            R.id.action_upload -> {
                                if(ActivityCompat.checkSelfPermission(this,
                                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_GRANTED){
                                    startActivity(Intent(this, AddPhotoActivity::class.java))
                                }else{
                                    if(ActivityCompat.shouldShowRequestPermissionRationale(
                                            this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                    ) { //이전 거부 이력 있을 경우, 원래는 권한 요청 이유 설명 필요
                                        ActivityCompat.requestPermissions(
                                            this,
                                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                            99)
                                    }else{
                                        ActivityCompat.requestPermissions(
                                            this,
                                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                            99)
                                    }
                                }
                                true
                            }
                            R.id.action_favorite -> {
                                fragmentManager.beginTransaction().replace(
                                    R.id.main_content, ActiveFragment()).commit()
                                binding.bottomNav.menu.getItem(4).isEnabled=true
                                true
                            }
                            R.id.action_account -> {
                                var accountFragment = AccountFragment()
                                var bundle = Bundle()
                                var uid = FirebaseAuth.getInstance().currentUser?.uid

                                bundle.putString("destinationUid", uid)
                                accountFragment.arguments = bundle

                                supportFragmentManager.beginTransaction().replace(
                                    R.id.main_content, accountFragment).commit()
                                binding.bottomNav.menu.getItem(4).isEnabled=false
                                true
                            }
                            else -> false
                        }
                    }
                }
            }




    }

    //권한요청
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            99 -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startActivity(Intent(this, AddPhotoActivity::class.java))
                    } else {
                        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)){
                            ActivityCompat.requestPermissions(this@MainActivity,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 99)
                        } else {
                            showDialogToGetPermission(this)
                        }
                    }
                }
            }
        }
    }

    private fun showDialogToGetPermission(context: Context){
        AlertDialog.Builder(this).setTitle("권한 요청")
            .setMessage("이미지 업로드 기능을 이용하려면 저장소 권한이 필요합니다")
            .setPositiveButton("확인"){ dialog, i ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton("취소", null)
            .create().show()
    }

}