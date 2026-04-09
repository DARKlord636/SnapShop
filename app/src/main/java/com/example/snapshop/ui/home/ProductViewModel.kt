package com.example.snapshop.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapshop.data.model.Product
import com.example.snapshop.data.repository.ProductRepository
import com.example.snapshop.utils.Resource
import com.example.snapshop.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {


    private val _products = MutableLiveData<Resource<List<Product>>>()
    val products: LiveData<Resource<List<Product>>> = _products


    private val _selectedProduct = SingleLiveEvent<Resource<Product>>()
    val selectedProduct: LiveData<Resource<Product>> = _selectedProduct

    init {
        fetchAllProducts()
    }

    fun fetchAllProducts() {
        viewModelScope.launch {
            _products.value = Resource.Loading
            _products.value = productRepository.getAllProducts()
        }
    }

    fun fetchProductById(productId: String) {
        viewModelScope.launch {
            _selectedProduct.value = Resource.Loading
            _selectedProduct.value = productRepository.getProductById(productId)
        }
    }
}