package com.example.snapshop.data.local.dao



import androidx.room.*
import com.example.snapshop.data.local.entity.FavoriteEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE productId = :productId")
    suspend fun getFavoriteById(productId: String): FavoriteEntity?

    @Query("DELETE FROM favorites WHERE productId = :productId")
    suspend fun deleteFavoriteById(productId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE productId = :productId)")
    fun isFavorite(productId: String): Flow<Boolean>
}