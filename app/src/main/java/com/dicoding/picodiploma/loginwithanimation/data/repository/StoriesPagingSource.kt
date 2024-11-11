package com.dicoding.picodiploma.loginwithanimation.data.repository

import ApiService
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.ListStoryItem
import kotlinx.coroutines.flow.firstOrNull

class StoriesPagingSource(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    private val location: Int? = null
) : PagingSource<Int, ListStoryItem>() {

    companion object {

        private const val INITIAL_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val token = userPreference.getToken().firstOrNull()

            if (token.isNullOrEmpty()) {
                return LoadResult.Error(Exception("Token not found"))
            }

            val response = location?.let {
                apiService.getStoriesAuth(
                    "Bearer $token",
                    page = position,
                    size = params.loadSize,
                    location = it
                )
            }
            val responseData = response?.listStory?.filterNotNull() ?: emptyList()

            LoadResult.Page(
                data = responseData,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseData.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}
