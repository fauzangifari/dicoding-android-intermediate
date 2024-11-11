import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.repository.StoriesRepository
import com.dicoding.picodiploma.loginwithanimation.data.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MainViewModel(
    private val userRepository: UserRepository,
    private val storiesRepository: StoriesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _uploadStatus = MutableStateFlow(false)
    val uploadStatus: StateFlow<Boolean> get() = _uploadStatus

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _newStory = MutableStateFlow<ListStoryItem?>(null)
    val newStory: StateFlow<ListStoryItem?> get() = _newStory

    fun getSession(): Flow<UserModel> = userRepository.getSession()

    fun getStoriesStream(location: Int): LiveData<PagingData<ListStoryItem>> {
        return storiesRepository.getStoriesStream(location).cachedIn(viewModelScope)
    }

    fun postStory(description: RequestBody, photo: MultipartBody.Part) {
        _isLoading.value = true
        viewModelScope.launch(dispatcher) {
            try {
                val response = storiesRepository.postStoryAuth(description, photo)
                _uploadStatus.value = true
                _errorMessage.value = "Story posted successfully"
                _newStory.value = response.listStory?.firstOrNull()
            } catch (e: Exception) {
                _uploadStatus.value = false
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getStoriesWithLocation(location: Int): Flow<List<ListStoryItem>> {
        return flow {
            val token = userRepository.getSession().firstOrNull()?.token
            if (!token.isNullOrEmpty()) {
                try {
                    val response = storiesRepository.getStories(token, location)
                    emit(response.listStory?.filterNotNull() ?: emptyList())
                } catch (e: Exception) {
                    emit(emptyList())
                }
            } else {
                emit(emptyList())
            }
        }.flowOn(dispatcher)
    }

    fun logout() {
        viewModelScope.launch(dispatcher) {
            userRepository.logout()
        }
    }
}
