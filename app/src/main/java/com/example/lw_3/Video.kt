package com.example.lw_3

data class Video (
    val id: Int,
    val name: String,
    val url: String,
    val comments: MutableList<String>,
    val sharedVideos: MutableList<Int>,
    var isLiked: Boolean = false,
    val authorId: Int = 1


)