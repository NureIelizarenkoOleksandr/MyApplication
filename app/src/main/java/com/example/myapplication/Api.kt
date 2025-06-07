package com.example.myapplication

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.http.Body
import retrofit2.http.POST

// --- Данные и API интерфейс ---

data class UserCreate(val email: String, val password: String, val username: String? = null)
data class LoginResponse(val access_token: String, val token_type: String)

interface ApiService {
    @GET("/routes/routes/test")
    suspend fun getMessage(): String

    @GET("/routes/routes")
    suspend fun getRoutes(@Query("page") page: Int = 1): RouteResponse

    @GET("/routes/routes/{id}")
    suspend fun getRouteById(@Path("id") id: Int): Route

    @POST("/register")
    suspend fun register(@Body user: UserCreate): Unit

    @POST("/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse
}

// --- Интерсептор для добавления токена в заголовок ---

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val originalRequest: Request = chain.request()

        val token = tokenProvider()

        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}


val tokenState: MutableState<String?> = mutableStateOf(null)


val apiService: ApiService by lazy {
    val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor { tokenState.value })
        .build()

    Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}
