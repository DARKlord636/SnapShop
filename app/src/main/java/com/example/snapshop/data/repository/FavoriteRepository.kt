package com.example.snapshop.data.repository

import com.example.snapshop.data.local.dao.FavoriteDao
import com.example.snapshop.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {

    fun getAllFavorites(): Flow<List<FavoriteEntity>> {
        return favoriteDao.getAllFavorites()
    }

    suspend fun addFavorite(favorite: FavoriteEntity) {
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun removeFavorite(favorite: FavoriteEntity) {
        favoriteDao.deleteFavorite(favorite)
    }

    suspend fun removeFavoriteById(productId: String) {
        favoriteDao.deleteFavoriteById(productId)
    }

    fun isFavorite(productId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(productId)
    }

    suspend fun getFavoriteById(productId: String): FavoriteEntity? {
        return favoriteDao.getFavoriteById(productId)
    }
}
