package com.durranitech.realtimechatapp.viewModel

import AuthRepository
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch


class AuthViewModel(private val repository: AuthRepository, val navController: NavController) : ViewModel() {

    private val _uiState = MutableLiveData<AuthUiState>()
    val uiState: LiveData<AuthUiState> get() = _uiState
    fun authenticateUser(
        email: String, password: String, context: Context
    ) {
        viewModelScope.launch {
            repository.checkEmailAndAuthenticate(email, password, _uiState)
            if (uiState.value is AuthUiState.Success) {
                navController.navigate("home")
            } else {
                navController.navigate("auth")
            }
        }
    }

    fun isUserAuthenticated(): Boolean{
        return repository.isUserAuthenticated()
    }
}



sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
