package com.dicoding.picodiploma.loginwithanimation.view.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.common.Resource
import com.dicoding.picodiploma.loginwithanimation.data.repository.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.remote.request.LoginRequest
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    fun loginUser(email: String, password: String, onResult: (Resource<LoginResponse?>) -> Unit) {
        viewModelScope.launch {
            onResult(Resource.Loading())
            try {
                val response = repository.loginUser(LoginRequest(email, password))
                response?.loginResult?.let {
                    saveSession(UserModel(it.name ?: "", it.token ?: ""))
                }
                onResult(Resource.Success(response))
            } catch (e: Exception) {
                onResult(Resource.Error("Login failed"))
            }
        }
    }


    private fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }
}