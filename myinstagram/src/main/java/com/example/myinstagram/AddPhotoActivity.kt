package com.example.myinstagram

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myinstagram.MyApplication.Companion.auth
import com.example.myinstagram.databinding.ActivityAddPhotoBinding
import com.example.myinstagram.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val openGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
            try {
                //샘플사이즈 이미지 계산
                val calRatio = calculateInSampleSize(
                    it.data!!.data!!,
                    resources.getDimensionPixelSize(R.dimen.img_size),
                    resources.getDimensionPixelSize(R.dimen.img_size)
                )
                val option = BitmapFactory.Options()
                option.inSampleSize = calRatio
                //이미지 로딩
                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
               //Uri 저장
                photoUri = it.data?.data
                val bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                inputStream!!.close()
                inputStream = null
                bitmap?.let{
                    binding.addedPhoto.setImageBitmap(bitmap)
                } ?: let{
                    Log.d("AddPhotoActivity", "bitmap null")
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }

        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        openGalleryLauncher.launch(galleryIntent)

        binding.addedPhoto.setOnClickListener {
            openGalleryLauncher.launch(galleryIntent)
        }

        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.uploadBtn.setOnClickListener {
            if(photoUri == null){
                Toast.makeText(this, R.string.warn_select_image, Toast.LENGTH_SHORT).show()
            }else if(binding.feedText.text.isBlank()){
                Toast.makeText(this, R.string.warn_type_text, Toast.LENGTH_SHORT).show()
            }else {
                contentUpload()
            }
        }
    }

    //업로드
    private fun contentUpload(){
        //파일명
        var fileName = "IMAGE_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".png"
        var storageRef = storage?.reference?.child("image")?.child(fileName)

        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl}?.addOnSuccessListener { uri->
                var contentDTO = ContentDTO()

                contentDTO.imageUri = uri.toString()
                contentDTO.uid = auth?.currentUser?.uid
                contentDTO.userId = auth?.currentUser?.email
                contentDTO.explain = findViewById<EditText>(R.id.feed_text).text.toString()
                contentDTO.timeStamp = System.currentTimeMillis()

            db?.collection("images")?.document()?.set(contentDTO)
                setResult(Activity.RESULT_OK)
                Toast.makeText(this, R.string.upload_success, Toast.LENGTH_SHORT).show()
                finish()
        }
    }
    //샘플사이징
    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            var inputStream = contentResolver.openInputStream(fileUri)

            //inJustDecodeBounds 값을 true 로 설정한 상태에서 decodeXXX() 를 호출.
            //로딩 하고자 하는 이미지의 각종 정보가 options 에 설정 된다.
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()
            inputStream = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //비율 계산........................
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        //inSampleSize 비율 계산
        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

}