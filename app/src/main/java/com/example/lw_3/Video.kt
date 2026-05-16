package com.example.lw_3

import com.google.gson.annotations.SerializedName

data class Video (
    val id: Int,
    val name: String,
    val url: String,
    val comments: MutableList<Comment> = mutableListOf(),

    @SerializedName("sharedvideos")
    val sharedvideos: MutableList<SharedVideo> = mutableListOf(),

    var likes: Int = 0,
    var isLiked: Boolean = false,
    val likedBy: MutableList<Int> = mutableListOf(),
    val author: Int = 1
)

data class Comment(
    var text: String = "",
    var author: String = ""
)

data class SharedVideo(
    var receiverId: Int = 0,
    var sharedBy: String = ""

)