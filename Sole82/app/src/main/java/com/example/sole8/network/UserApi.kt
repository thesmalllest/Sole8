package com.example.sole8.network

import com.example.sole8.models.api.*
import retrofit2.http.*

interface UserApi {

    @POST("api/user/login")
    suspend fun login(@Body req: UserLoginRequest): LoginResponse

    @POST("api/user/register")
    suspend fun register(@Body req: UserRegisterRequest): RegisterResponse

    @GET("api/user/profile")
    suspend fun getProfile(): UserProfileDto

    @PUT("api/user/profile")
    suspend fun updateProfile(
        @Body req: UserProfileUpdate
    ): SimpleResponse
}