package com.example.sole8.models.api

data class ProductListDto(
    val id: Int,
    val name: String,
    val brand: String,
    val price: Double,
    val thumbnailUrl: String,
    val model3DUrl: String?
)

data class ProductSizeDto(
    val id: Int,
    val sizeValue: Double,
    val stock: Int
)

data class ProductDetailsDto(
    val id: Int,
    val name: String,
    val brand: String,
    val price: Double,
    val description: String,
    val model3DUrl: String?,
    val images: List<String>,
    val sizes: List<ProductSizeDto>
)
