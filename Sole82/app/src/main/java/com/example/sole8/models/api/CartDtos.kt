package com.example.sole8.models.api

data class CartItemDto(
    val id: Int,
    val productId: Int,
    val sizeId: Int,
    val quantity: Int,
    val name: String,
    val price: Double,
    val sizeValue: Double,
    val stock: Int,
    val imageUrl: String
)