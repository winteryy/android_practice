package com.example.myinstagram

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myinstagram.navigation.model.ContentDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountFragment : Fragment() {
    var fragmentView: View? = null
    var fireStore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_account, container, false)
        uid = arguments?.getString("destinationUid")
        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserId = auth?.currentUser?.uid
        var userButton = fragmentView!!.findViewById<Button>(R.id.follow_button)

        //본인 페이지인지 타인 페이지인지 확인
        if(uid == currentUserId){
            userButton.text = getString(R.string.signOut_button)
            userButton.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
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
        }

        val profileRecyclerView = fragmentView?.findViewById<RecyclerView>(R.id.profile_recycler_view)
        profileRecyclerView?.adapter = UserFragmentRecyclerViewAdapter()
        profileRecyclerView?.layoutManager = GridLayoutManager(requireActivity(), 3)
        profileRecyclerView?.addItemDecoration(Spacing())

        fragmentView?.findViewById<ImageView>(R.id.account_profile_image)?.setOnClickListener {

        }

        return fragmentView
    }

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
                    for (snapShot in value?.documents!!){
                        contentDTOs.add(snapShot.toObject(ContentDTO::class.java)!!)
                    }

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
//구현해야 하는 것, 해당유저 게시물 리사이클러뷰(그리드), 동적으로 주고받는 팔로우 관련 것들, 유저데이터 DTO(프로필 이미지 등),
// 본인인지 타인인지에 따른 레이아웃이나 기능 부여(팔로우버튼 등)
}