package com.example.snapshop.ui.upload

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapshop.data.model.Product
import com.example.snapshop.data.repository.AuthRepository
import com.example.snapshop.data.repository.ProductRepository
import com.example.snapshop.utils.Resource
import com.example.snapshop.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uploadState = SingleLiveEvent<Resource<String>>()
    val uploadState: LiveData<Resource<String>> = _uploadState

    private val _selectedImages = MutableLiveData<List<Uri>>(emptyList())
    val selectedImages: LiveData<List<Uri>> = _selectedImages

    fun setSelectedImages(uris: List<Uri>) {
        _selectedImages.value = uris
    }

    fun uploadProduct(
        title: String,
        shortDescription: String,
        description: String,
        price: Double,
        uploaderName: String,
        uploaderContact: String
    ) {
        val currentUser = authRepository.currentUser
        if (currentUser == null) {
            _uploadState.value = Resource.Error("User not logged in")
            return
        }

        val images = _selectedImages.value ?: emptyList()
        if (images.size < 3) {
            _uploadState.value = Resource.Error("Please select at least 3 images")
            return
        }

        viewModelScope.launch {
            _uploadState.value = Resource.Loading
            val product = Product(
                title = title,
                shortDescription = shortDescription,
                description = description,
                price = price,
                uploaderId = currentUser.uid,
                uploaderName = uploaderName,
                uploaderContact = uploaderContact
            )
            _uploadState.value = productRepository.uploadProduct(product, images)
        }
    }
}