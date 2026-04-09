package com.example.snapshop.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.snapshop.data.local.entity.FavoriteEntity
import com.example.snapshop.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    val favorites: LiveData<List<FavoriteEntity>> =
        favoriteRepository.getAllFavorites().asLiveData()

    fun addFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            favoriteRepository.addFavorite(favorite)
        }
    }

    fun removeFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(favorite)
        }
    }

    fun removeFavoriteById(productId: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavoriteById(productId)
        }
    }

    fun isFavorite(productId: String): LiveData<Boolean> {
        return favoriteRepository.isFavorite(productId).asLiveData()
    }
}
