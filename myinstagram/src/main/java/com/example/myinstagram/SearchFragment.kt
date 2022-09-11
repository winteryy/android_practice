package com.example.myinstagram

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myinstagram.databinding.FragmentSearchBinding
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : Fragment() {
    var fireStore: FirebaseFirestore ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSearchBinding.inflate(layoutInflater)
        fireStore = FirebaseFirestore.getInstance()

        binding.searchInputText.setOnEditorActionListener { textView, i, keyEvent ->
            if(i == EditorInfo.IME_ACTION_SEARCH){
                searchUser(binding.searchInputText.text.toString())
                true
            }
            false
        }
        binding.searchInputButton.setOnClickListener {
            searchUser(binding.searchInputText.text.toString())
        }
        return binding.root
    }

    private fun searchUser(str: String){
        if(str.length>=2) {
            val userInfo = fireStore?.collection("userInfo")?.whereEqualTo("userName", str)
            userInfo?.get()?.addOnSuccessListener {
                var bundle = Bundle()
                var accountFragment = AccountFragment()
                if(it.size() == 0) {
                    Toast.makeText(context, "일치하는 유저가 없습니다", Toast.LENGTH_SHORT).show()
                }else if(it.size() == 1){
                    for (user in it) {
                        bundle.putString("destinationUid", user?.get("uid").toString())
                        bundle.putString("userId", user?.get("userName").toString())
                    }
                    accountFragment.arguments = bundle
                    (activity as MainActivity).supportFragmentManager.beginTransaction()?.replace(
                        R.id.main_content, accountFragment)?.commit()
                }
            }
        }else{
            Toast.makeText(context, "유저명을 2자 이상 입력해주세요", Toast.LENGTH_SHORT).show()
        }
    }
}