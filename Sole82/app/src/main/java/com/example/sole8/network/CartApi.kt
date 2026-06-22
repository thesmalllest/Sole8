package com.example.sole8.network

import com.example.sole8.models.api.CartItemDto
import com.example.sole8.models.api.SimpleResponse
import retrofit2.http.*

interface CartApi {

    @GET("api/cart")
    suspend fun getCart(): List<CartItemDto>

    @POST("api/cart/add")
    suspend fun addToCart(
        @Query("productId") productId: Int,
        @Query("sizeId") sizeId: Int,
        @Query("quantity") quantity: Int
    ): SimpleResponse

    @POST("api/cart/update")
    suspend fun updateCart(
        @Query("productId") productId: Int,
        @Query("sizeId") sizeId: Int,
        @Query("quantity") quantity: Int
    ): SimpleResponse

    @POST("api/cart/remove")
    suspend fun removeFromCart(
        @Query("productId") productId: Int,
        @Query("sizeId") sizeId: Int
    ): SimpleResponse

    @POST("api/cart/clear")
    suspend fun clearCart(): SimpleResponse
}