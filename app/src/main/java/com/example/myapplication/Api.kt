package com.example.myapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @GET("/routes/routes/test")
    suspend fun getMessage(): String

    @GET("/routes/routes")
    suspend fun getRoutes(@Query("page") page: Int = 1): RouteResponse

    @GET("/routes/routes/{id}")
    suspend fun getRouteById(@Path("id") id: Int): Route
}

val apiService: ApiService = Retrofit.Builder()
    .baseUrl("http://10.0.2.2:8000/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(ApiService::class.java)
