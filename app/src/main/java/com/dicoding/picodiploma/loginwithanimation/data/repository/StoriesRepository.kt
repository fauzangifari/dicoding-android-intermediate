package com.dicoding.picodiploma.loginwithanimation.data.repository

import ApiService
import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.StoriesResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoriesRepository private constructor(
    private val preference: UserPreference,
    private val apiService: ApiService
) : IStoriesRepository {

    override fun getStoriesStream(location: Int?): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                StoriesPagingSource(preference, apiService, location)
            }
        ).liveData
    }

    override suspend fun postStoryAuth(description: RequestBody, photoFile: MultipartBody.Part) =
        try {
            val token = getToken() ?: throw Exception("Token not found. Please log in again.")
            apiService.postStoryAuth("Bearer $token", description, photoFile)
        } catch (e: Exception) {
            throw Exception("Failed to post story: ${e.message}")
        }

    override suspend fun getStories(token: String, location: Int): StoriesResponse {
        return apiService.getStoriesAuth("Bearer $token", page = 1, size = 15, location = location)
    }

    private suspend fun getToken(): String? {
        return preference.getToken().firstOrNull()
    }

    companion object {
        @Volatile
        private var instance: StoriesRepository? = null

        fun getInstance(preference: UserPreference, apiService: ApiService): StoriesRepository =
            instance ?: synchronized(this) {
                instance ?: StoriesRepository(preference, apiService)
            }.also { instance = it }
    }
}
