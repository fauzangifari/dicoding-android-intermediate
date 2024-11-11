import com.dicoding.picodiploma.loginwithanimation.data.remote.request.LoginRequest
import com.dicoding.picodiploma.loginwithanimation.data.remote.request.RegisterRequest
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.RegisterResponse
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    @POST("login")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): LoginResponse

    @POST("register")
    suspend fun registerUser(
        @Body registerRequest: RegisterRequest
    ): RegisterResponse

    @Multipart
    @POST("stories")
    suspend fun postStoryAuth(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part
    ): StoriesResponse

    @GET("stories")
    suspend fun getStoriesAuth(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("location") location: Int
    ): StoriesResponse
}
