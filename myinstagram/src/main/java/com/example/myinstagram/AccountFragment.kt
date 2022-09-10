package com.example.myinstagram

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myinstagram.navigation.model.ContentDTO
import com.example.myinstagram.navigation.model.FollowDTO
import com.example.myinstagram.navigation.model.LoadingDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class AccountFragment : Fragment() {
    var fragmentView: View? = null
    var fireStore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserId: String? = null
    var profileImgCheck: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_account, container, false)
        uid = arguments?.getString("destinationUid")
        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserId = auth?.currentUser?.uid

        return fragmentView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var userButton = fragmentView!!.findViewById<Button>(R.id.follow_button)

        //본인 페이지인지 타인 페이지인지 확인
        if(uid == currentUserId){
            userButton.text = getString(R.string.signOut_button)
            userButton.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
            //프로필 이미지 클릭시
            fragmentView?.findViewById<ImageView>(R.id.account_profile_image)?.setOnClickListener {
                if(ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                    val builder = makeSelectImageDialogBuilder()
                    builder.show()
                }else{
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }else{
            userButton.text = getString(R.string.follow_button)

            var mainActivity = (activity as MainActivity)
            mainActivity.findViewById<TextView>(R.id.bar_userName)!!.text = requireArguments().getString("userId")
            mainActivity.findViewById<ImageView>(R.id.bar_back_button)!!.setOnClickListener {
                mainActivity.findViewById<BottomNavigationView>(R.id.bottom_nav).selectedItemId = R.id.action_home
            }
            mainActivity.findViewById<TextView>(R.id.bar_userName).visibility = View.VISIBLE
            mainActivity.findViewById<ImageView>(R.id.bar_back_button).visibility = View.VISIBLE
            mainActivity.findViewById<ImageView>(R.id.logo).visibility = View.GONE
            userButton?.setOnClickListener {
                requestFollow()
            }
            fragmentView?.findViewById<ImageView>(R.id.account_profile_image)?.setOnClickListener{}
        }

        val profileRecyclerView = fragmentView?.findViewById<RecyclerView>(R.id.profile_recycler_view)
        profileRecyclerView?.adapter = UserFragmentRecyclerViewAdapter()
        profileRecyclerView?.layoutManager = GridLayoutManager(requireActivity(), 3)
        profileRecyclerView?.addItemDecoration(Spacing())

        updateProfileImage()
        getFollowerAndFollowing()
        super.onViewCreated(view, savedInstanceState)
    }

    //리사이클러뷰(피드) 구성
    inner class Spacing: RecyclerView.ItemDecoration(){
        val padding = 2

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.set(padding,padding,padding,padding)
        }
    }

    inner class UserFragmentRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        init{
            fireStore?.collection("images")?.document(uid!!)
                ?.collection("feed")?.addSnapshotListener { value, error ->
                    if(value == null){
                        return@addSnapshotListener
                    }
                    contentDTOs.clear()
                    for (snapShot in value.documents){
                        contentDTOs.add(snapShot.toObject(ContentDTO::class.java)!!)
                    }
                    contentDTOs.sortByDescending { it.timeStamp }
                    fragmentView?.findViewById<Button>(R.id.profile_button_feed)
                        ?.text = contentDTOs.size.toString() + "\n게시물"

                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val width = resources.displayMetrics.widthPixels / 3 - 6
            val imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayout.LayoutParams(width, width)

            return CustomViewHolder(imageView)
        }
        inner class CustomViewHolder(var imageView: ImageView): RecyclerView.ViewHolder(imageView)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUri)
                .apply(RequestOptions().centerCrop()).into(imageView)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }

    
    //프로필 이미지 업로드 권한 관련
    private fun showDialogToGetPermission(context: Context) {
        AlertDialog.Builder(context).setTitle("권한 요청")
            .setMessage("이미지 업로드 기능을 이용하려면 저장소 권한이 필요합니다")
            .setPositiveButton("확인") { dialog, i ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton("취소", null)
            .create().show()
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted){
            val builder = makeSelectImageDialogBuilder()
            builder.show()
        }else{
            showDialogToGetPermission(requireActivity())
        }
    }

    //갤러리 연동
    private val openGalleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
            var profile_image_uri = it.data?.data
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(currentUserId!!)
            val loadingDialog = LoadingDialog(requireContext())
            loadingDialog.show()
            storageRef.putFile(profile_image_uri!!).continueWithTask { task: com.google.android.gms.tasks.Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri ->
                fireStore?.collection("userInfo")?.document(currentUserId!!)?.set(
                    hashMapOf("profile_img" to uri.toString()), SetOptions.merge())
                updateProfileImage()
                loadingDialog.dismiss()
            }
    }
    
    //프로필 이미지 설정 다이얼로그 빌더 생성
    private fun makeSelectImageDialogBuilder(): AlertDialog.Builder {
        var builder : AlertDialog.Builder = AlertDialog.Builder(requireContext())
        var optionArray: Array<String>
        if (profileImgCheck==0){
            optionArray = arrayOf("갤러리에서 선택")
        }else{
            optionArray = arrayOf("갤러리에서 선택", "기본 이미지로 변경")
        }

        builder.setTitle("프로필 이미지 설정").setItems(optionArray,
            DialogInterface.OnClickListener { dialogInterface, i ->
                when(i){
                    0 -> {
                        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galleryIntent.type = "image/*"
                        openGalleryLauncher.launch(galleryIntent)
                    }
                    1 -> {
                        var storageRef = FirebaseStorage.getInstance().reference
                            .child("userProfileImages").child(currentUserId!!)
                        storageRef.delete().addOnSuccessListener {
                            fireStore?.collection("userInfo")?.document(currentUserId!!)
                                ?.update(hashMapOf<String, Any>("profile_img" to FieldValue.delete()))
                                ?.addOnSuccessListener { updateProfileImage() }

                        }
                    }
                }
            })
        return builder
    }

    private fun updateProfileImage() : Unit {
        if(activity == null){
            return
        }else {
            var profile_image = fragmentView?.findViewById<ImageView>(R.id.account_profile_image)
            fireStore?.collection("userInfo")?.document(uid!!)?.get()?.addOnSuccessListener {
                if (it.data?.get("profile_img") == null) {
                    profileImgCheck = 0
                    profile_image?.setImageResource(R.drawable.user_basic)
                } else {
                    profileImgCheck = 1
                    Glide.with(requireActivity()).load(it.data?.get("profile_img"))
                        .apply(RequestOptions().centerCrop()).into(profile_image!!)
                }
            }
        }
    }

    //팔로우 처리
    private fun requestFollow(){
        var tsDocFollowing = fireStore!!.collection("followInfo").document(currentUserId!!)
        fireStore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if (followDTO == null){
                followDTO = FollowDTO()
                followDTO.followingCount = 1
                followDTO.followings[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction
            }

            if(followDTO?.followings?.containsKey(uid)!!){
                followDTO?.followingCount = followDTO?.followingCount!! - 1
                followDTO?.followings!!.remove(uid)
            }else {
                followDTO?.followingCount = followDTO?.followingCount!! + 1
                followDTO?.followings!![uid!!] = true
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        var tsDocFollower = fireStore!!.collection("followInfo").document(uid!!)
        fireStore?.runTransaction{  transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserId!!] = true

                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }

            if (followDTO!!.followers.containsKey(currentUserId!!)){
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserId!!)
            }else{
                followDTO!!.followerCount = followDTO!!.followingCount + 1
                followDTO!!.followers[currentUserId!!] = true
            }
            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }
    }

    private fun getFollowerAndFollowing(){
        fireStore?.collection("followInfo")?.document(uid!!)?.addSnapshotListener{ docSnapShot, e ->
            if(docSnapShot==null) return@addSnapshotListener

            var followDTO = docSnapShot.toObject(FollowDTO::class.java)

            if(followDTO?.followingCount != null){
                fragmentView?.findViewById<Button>(R.id.profile_button_following)?.text =
                    followDTO?.followingCount?.toString() + "\n팔로잉"
            }
            if(followDTO?.followerCount != null){
                fragmentView?.findViewById<Button>(R.id.profile_button_follower)?.text =
                    followDTO?.followerCount?.toString() + "\n팔로워"

                if(followDTO?.followers?.containsKey(currentUserId)!!){
                    fragmentView?.findViewById<Button>(R.id.follow_button)?.text =
                        activity?.getString(R.string.follow_cancel_button)
                }else{
                    if(uid != currentUserId){
                        fragmentView?.findViewById<Button>(R.id.follow_button)?.text =
                            activity?.getString(R.string.follow_button)
                    }
                }
            }
        }
    }

}