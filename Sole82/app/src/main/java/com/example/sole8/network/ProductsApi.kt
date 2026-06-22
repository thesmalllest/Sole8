package com.example.sole8.network

import com.example.sole8.models.api.ProductListDto
import com.example.sole8.models.api.ProductDetailsDto
import retrofit2.http.*

interface ProductsApi {

    @GET("api/products")
    suspend fun getProducts(): List<ProductListDto>

    @GET("api/products/{id}")
    suspend fun getProductDetails(@Path("id") id: Int): ProductDetailsDto
}
