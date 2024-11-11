package com.dicoding.picodiploma.loginwithanimation.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.StoriesResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface IStoriesRepository {
    fun getStoriesStream(location: Int?): LiveData<PagingData<ListStoryItem>>
    suspend fun postStoryAuth(description: RequestBody, photoFile: MultipartBody.Part): StoriesResponse
    suspend fun getStories(token: String, location: Int): StoriesResponse
}
