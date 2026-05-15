package com.example.lw_3

data class User (
    val id: Int = 0,
    val name: String = "",
    val role: String = "user",
    val password: String = "",

){
    val isAdmin: Boolean get() = role == "admin"
}