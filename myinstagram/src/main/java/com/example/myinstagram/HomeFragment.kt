package com.example.myinstagram

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myinstagram.databinding.FragmentHomeBinding
import com.example.myinstagram.databinding.ItemDetailBinding
import com.example.myinstagram.navigation.model.ContentDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class HomeFragment : Fragment() {
    var fireStore: FirebaseFirestore? = null
    var uid: String? = null
    var fireStorage: FirebaseStorage? = null
    //더블 탭용
    var initNum = -1
    var initTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fireStore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        initNum = -1
        initTime = 0L

        val binding = FragmentHomeBinding.inflate(layoutInflater)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.mainRecyclerView.adapter = DetailViewRecyclerViewAdapter()
        binding.refresh.setOnRefreshListener {
            binding.refresh.isRefreshing=false
            binding.mainRecyclerView.adapter = DetailViewRecyclerViewAdapter()
        }
        return binding.root
    }

    inner class DetailViewRecyclerViewAdapter:
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()
        var followingList: ArrayList<String> = arrayListOf()
        init {
            fireStore?.collection("followInfo")?.document(uid!!)?.get()?.addOnSuccessListener {
                if(it.contains("followings")) {
                    for ((key, value) in (it.data!!["followings"] as HashMap<String, Boolean>)) {
                        followingList.add(key)
                    }
                    followingList.add(uid!!)
                    loadFeedFromFb()
                }else{
                    followingList.add(uid!!)
                    loadFeedFromFb()
                }
            }?.addOnFailureListener {
                followingList.add(uid!!)
                loadFeedFromFb()
            }
        }

        private fun loadFeedFromFb(){
            for(following in followingList) {
                fireStore?.collection("images")?.document(following)
                    ?.collection("feed")?.orderBy("timeStamp", Query.Direction.DESCENDING)
                    ?.addSnapshotListener { result, e ->
                        if(!result?.isEmpty!!) {
                            contentDTOs = contentDTOs.filter {
                                it.uid != result!!.documents[0]?.get("uid")
                            } as ArrayList<ContentDTO>
                        }
                        for (document in result!!.documents) {
                            var item = document.toObject(ContentDTO::class.java)
                            contentDTOs.add(item!!)
                        }
                        contentDTOs.sortByDescending { it.timeStamp }
                        notifyDataSetChanged()
                    }
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            CustomViewHolder(ItemDetailBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false
                )
            )
        inner class CustomViewHolder(val binding: ItemDetailBinding): RecyclerView.ViewHolder(binding.root)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val binding = (holder as CustomViewHolder).binding

            fireStore?.collection("userInfo")?.document(contentDTOs[position].uid!!)
                ?.get()?.addOnSuccessListener {
                if (it.data?.get("profile_img") == null) {
                    binding.userProfileImage?.setImageResource(R.drawable.user_basic)
                } else {
                    var cont = context
                    if (cont != null && isAdded) {
                        Glide.with(requireContext()).load(it.data?.get("profile_img"))
                            .apply(RequestOptions().centerCrop()).into(binding.userProfileImage)
                    }
                }
            }

            binding.userProfileName.setOnClickListener {
                if(uid==contentDTOs[position].uid) {
                    activity!!.findViewById<BottomNavigationView>(R.id.bottom_nav)
                        .selectedItemId = R.id.action_account
                }else {
                    val fragment = AccountFragment()
                    val bundle = Bundle()

                    bundle.putString("destinationUid", contentDTOs[position].uid)
                    bundle.putString("userId", contentDTOs[position].userId)
                    fragment.arguments = bundle
                    activity!!.supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, fragment)
                        .commit()
                }
            }
            binding.userProfileImage.setOnClickListener {
                if(uid==contentDTOs[position].uid) {
                    activity!!.findViewById<BottomNavigationView>(R.id.bottom_nav)
                        .selectedItemId = R.id.action_account
                }else {
                    val fragment = AccountFragment()
                    val bundle = Bundle()

                    bundle.putString("destinationUid", contentDTOs[position].uid)
                    bundle.putString("userId", contentDTOs[position].userId)
                    fragment.arguments = bundle
                    activity!!.supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, fragment)
                        .commit()
                }
            }

            //좋아요
            binding.likeButton.setOnClickListener {
                favoriteEvent(position)
            }

            binding.detailItemImage.setOnClickListener {
                if(initNum == position){
                    if (System.currentTimeMillis()-initTime > 300){
                        initTime = System.currentTimeMillis()
                    }else{
                        favoriteEvent(position)
                        initNum = -1
                        initTime = 0L
                    }
                }else{
                    initNum = position
                    initTime = System.currentTimeMillis()
                }
            }

            if(contentDTOs!![position].favorite.containsKey(uid)){
                binding.likeButton.setImageResource(R.drawable.like_on_icon)
            }else{
                binding.likeButton.setImageResource(R.drawable.like_off_icon)
            }

            //피드 메뉴
            if(contentDTOs!![position].uid != uid){
                binding.feedMenu.visibility = View.INVISIBLE
            }else{
                binding.feedMenu.visibility = View.VISIBLE
                binding.feedMenu.setOnMenuItemClickListener { 
                    when(it.itemId){
                        R.id.action_delete -> {
                            AlertDialog.Builder(context!!).setTitle("게시물 삭제")
                                .setMessage("게시물을 삭제하시겠습니까?")
                                .setPositiveButton("확인"){ dialog, i ->
                                    deleteContent(position)
                                }
                                .setNegativeButton("취소", null)
                                .create().show()
                            true
                        }
                        else -> false
                    }
                }
            }
            //내용받아오기
            binding.userProfileName.text = contentDTOs!![position].userId
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUri).
                    into(binding.detailItemImage)
            binding.detailItemExplain.text = contentDTOs!![position].explain
            binding.detailItemLikes.text = "좋아요 " + contentDTOs!![position].favoriteCount + "개"
            //프로필이미지 구현할 것
        }
        private fun favoriteEvent(position: Int){
            var tsDoc =
                fireStore?.collection("images")?.document(contentDTOs[position].uid!!)
                    ?.collection("feed")?.document(contentDTOs[position].imageName!!)

            fireStore?.runTransaction{
                transaction ->
                    var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorite.containsKey(uid)){
                    contentDTO.favoriteCount = contentDTO.favoriteCount-1
                    contentDTO.favorite.remove(uid)
                }else{
                    contentDTO.favoriteCount = contentDTO.favoriteCount+1
                    contentDTO.favorite[uid!!] = true
                }
                transaction.set(tsDoc, contentDTO)
            }

        }

        private fun deleteContent(position: Int){
            fireStorage = FirebaseStorage.getInstance()
            fireStorage?.reference?.child("image/${contentDTOs[position].imageName}")
                ?.delete()

            fireStore?.collection("images")?.document(contentDTOs[position].uid!!)
                ?.collection("feed")
                ?.document(contentDTOs[position].imageName!!)?.delete()?.addOnSuccessListener {
                Toast.makeText(
                    context, R.string.delete_success,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}