package com.example.snapshop.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val contact: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val photoUrl: String = ""
)