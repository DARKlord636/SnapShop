package com.example.snapshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val productId: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val uploaderId: String,
    val uploaderName: String,
    val uploaderContact: String
)