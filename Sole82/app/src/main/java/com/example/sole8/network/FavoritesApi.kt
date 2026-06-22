package com.example.sole8.network

import com.example.sole8.models.api.FavoriteRequest
import com.example.sole8.models.api.ProductListDto
import com.example.sole8.models.api.SimpleResponse
import retrofit2.http.*

interface FavoritesApi {

    @GET("api/favorites")
    suspend fun getFavorites(): List<ProductListDto>

    @POST("api/favorites/add")
    suspend fun addToFavorites(@Body req: FavoriteRequest): SimpleResponse

    @HTTP(method = "DELETE", path = "api/favorites/remove", hasBody = true)
    suspend fun removeFromFavorites(@Body req: FavoriteRequest): SimpleResponse
}
