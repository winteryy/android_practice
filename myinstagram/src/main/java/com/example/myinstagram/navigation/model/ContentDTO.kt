package com.example.myinstagram.navigation.model

data class ContentDTO(var explain: String? = null, var imageUri: String? = null,
                      var uid: String? = null, var userId: String? = null, var timeStamp: Long? = null,
                      var favoriteCount: Int = 0, var favorite: MutableMap<String, Boolean> = HashMap())
{
    data class Comment(var uid: String? = null, var userId: String? = null,
        var comment: String? = null, var timeStamp: Long? = null)
}