package com.durranitech.realtimechatapp.viewModel

import AuthRepository
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.Uri
import com.durranitech.realtimechatapp.data.model.User
import com.durranitech.realtimechatapp.data.repository.UserRepository
import com.durranitech.realtimechatapp.data.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<User>>(Resource.Idle())
    val profileState: StateFlow<Resource<User>> = _profileState

    private val _updateState = MutableStateFlow<Resource<String>>(Resource.Idle())
    val updateState: StateFlow<Resource<String>> = _updateState

    private val _uploadState = MutableStateFlow<Resource<String>>(Resource.Idle())
    val uploadState: StateFlow<Resource<String>> = _uploadState

    init {
        loadUserProfile()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = Resource.Loading()  // or Resource.Loading<User>()

            val userId = authRepository.getCurrentUserId()
            if (userId.isNotEmpty()) {
                _profileState.value = userRepository.getUserProfile(userId)
            } else {
                _profileState.value = Resource.Error<User>("No authenticated user found")
            }
        }
    }

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            val currentUser = (_profileState.value as? Resource.Success)?.data ?: return@launch

            _updateState.value = Resource.Loading()

            val updatedUser = currentUser.copy(userName = newName)
            _updateState.value = userRepository.updateUserProfile(updatedUser)

            if (_updateState.value is Resource.Success) {
                _profileState.value = Resource.Success(updatedUser)
            }
        }
    }

    fun uploadProfileImage(imageUri: Any) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId.isEmpty()) {
                _uploadState.value = Resource.Error("No authenticated user found")
                return@launch
            }

            _uploadState.value = Resource.Loading()

            val uploadResult = userRepository.uploadProfileImage(userId, imageUri as Uri)
            _uploadState.value = uploadResult

            if (uploadResult is Resource.Success) {
                val currentUser = (_profileState.value as? Resource.Success)?.data ?: return@launch
                val updatedUser = currentUser.copy(imageUrl = uploadResult.data ?: "")

                _updateState.value = Resource.Loading()
                _updateState.value = userRepository.updateUserProfile(updatedUser)

                if (_updateState.value is Resource.Success) {
                    _profileState.value = Resource.Success(updatedUser)
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}