package com.example.myinstagram

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myinstagram.databinding.FragmentHomeBinding
import com.example.myinstagram.databinding.ItemDetailBinding
import com.example.myinstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {
    var fireStore: FirebaseFirestore? = null
    var uid: String? = null
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
        return binding.root
    }

    inner class DetailViewRecyclerViewAdapter:
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init{
            fireStore?.collection("images")?.orderBy("timeStamp", Query.Direction.DESCENDING)?.
                    addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                        contentDTOs.clear()
                        contentUidList.clear()
                        for(snapShot in querySnapshot!!.documents){
                            var item = snapShot.toObject(ContentDTO::class.java)
                            contentDTOs.add(item!!)
                            contentUidList.add(snapShot.id)
                        }
                        notifyDataSetChanged()
                    }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            CustomViewHolder(ItemDetailBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))
        inner class CustomViewHolder(val binding: ItemDetailBinding): RecyclerView.ViewHolder(binding.root)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val binding = (holder as CustomViewHolder).binding

            //좋아요
            binding.likeButton.setOnClickListener {
                favoriteEvent(position)
            }

            binding.detailItemImage.setOnClickListener {
                if(initNum == position){
                    if (System.currentTimeMillis()-initTime > 1000){
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
                fireStore?.collection("images")?.document(contentUidList[position])
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
    }

}