package com.dicoding.picodiploma.loginwithanimation.di

import ApiService
import android.content.Context
import com.dicoding.picodiploma.loginwithanimation.data.repository.StoriesRepository
import com.dicoding.picodiploma.loginwithanimation.data.repository.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.data.remote.retrofit.ApiClient

object Injection {
    private fun provideUserPreference(context: Context): UserPreference {
        val dataStore = context.dataStore
        return UserPreference.getInstance(dataStore)
    }

    private fun provideApiService(): ApiService {
        return ApiClient.getApiService()
    }

    fun provideUserRepository(context: Context): UserRepository {
        val pref = provideUserPreference(context)
        val apiService = provideApiService()
        return UserRepository.getInstance(pref, apiService)
    }

    fun provideStoriesRepository(context: Context): StoriesRepository {
        val pref = provideUserPreference(context)
        val apiService = provideApiService()
        return StoriesRepository.getInstance(pref, apiService)
    }
}