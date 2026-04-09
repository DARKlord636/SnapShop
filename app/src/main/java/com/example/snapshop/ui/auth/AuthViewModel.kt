package com.example.snapshop.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapshop.data.model.User
import com.example.snapshop.data.repository.AuthRepository
import com.example.snapshop.utils.Resource
import com.example.snapshop.utils.SingleLiveEvent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {


    private val _loginState = SingleLiveEvent<Resource<Boolean>>()
    val loginState: LiveData<Resource<Boolean>> = _loginState

    private val _registerState = SingleLiveEvent<Resource<Boolean>>()
    val registerState: LiveData<Resource<Boolean>> = _registerState

    private val _googleSignInState = SingleLiveEvent<Resource<Boolean>>()
    val googleSignInState: LiveData<Resource<Boolean>> = _googleSignInState


    private val _userProfile = MutableLiveData<Resource<User>>()
    val userProfile: LiveData<Resource<User>> = _userProfile

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn


    fun validateEmail(email: String): Pair<Boolean, String> {
        return authRepository.isEmailValid(email)
    }


    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            val result = authRepository.loginUser(email, password)
            when (result) {
                is Resource.Success -> _loginState.value = Resource.Success(true)
                is Resource.Error   -> _loginState.value = Resource.Error(result.message)
                else -> Unit
            }
        }
    }


    fun registerUser(name: String, email: String, password: String, contact: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading
            val result = authRepository.registerUser(name, email, password, contact)
            when (result) {
                is Resource.Success -> _registerState.value = Resource.Success(true)
                is Resource.Error   -> _registerState.value = Resource.Error(result.message)
                else -> Unit
            }
        }
    }


    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _googleSignInState.value = Resource.Loading
            val result = authRepository.signInWithGoogle(account)
            when (result) {
                is Resource.Success -> _googleSignInState.value = Resource.Success(true)
                is Resource.Error   -> _googleSignInState.value = Resource.Error(result.message)
                else -> Unit
            }
        }
    }

    fun signUpWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _googleSignInState.value = Resource.Loading
            val result = authRepository.signUpWithGoogle(account)
            when (result) {
                is Resource.Success -> _googleSignInState.value = Resource.Success(true)
                is Resource.Error   -> _googleSignInState.value = Resource.Error(result.message)
                else -> Unit
            }
        }
    }


    fun loadUserProfile() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _userProfile.value = Resource.Loading
            val result = authRepository.getUserProfile(userId)
            _userProfile.value = result
        }
    }


    fun logoutUser() {
        authRepository.logoutUser()
    }
}