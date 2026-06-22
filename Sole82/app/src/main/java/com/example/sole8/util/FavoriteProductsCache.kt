package com.example.sole8.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object FavoriteProductsCache {

    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites: StateFlow<Set<Int>> get() = _favorites

    var loadedOnce = false

    fun isFavorite(id: Int): Boolean = _favorites.value.contains(id)

    fun setFavorites(ids: List<Int>) {
        _favorites.value = ids.toSet()
    }

    fun add(id: Int) {
        _favorites.value = _favorites.value + id
    }

    fun remove(id: Int) {
        _favorites.value = _favorites.value - id
    }
}
