package com.example.sole8

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.text.TextWatcher
import android.text.Editable
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sole8.adapters.ProductListAdapter
import com.example.sole8.network.ApiClient
import com.example.sole8.util.FavoriteProductsCache
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class CatalogActivity : BaseDrawerActivity() {

    private lateinit var prefs: UserPreferences
    private lateinit var searchView: SearchView
    private lateinit var recycler: RecyclerView
    private lateinit var brandSpinner: Spinner
    private lateinit var sortSpinner: Spinner
    private lateinit var etPriceMin: EditText
    private lateinit var etPriceMax: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_catalog, findViewById(R.id.content_frame))

        prefs = UserPreferences(this)

        attachHeader(R.id.logo)
        showBottomBar()

        val toolbar = findViewById<Toolbar>(R.id.catalogToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        recycler = findViewById(R.id.recyclerCatalog)
        recycler.layoutManager = LinearLayoutManager(this)

        searchView = findViewById(R.id.searchViewCatalog)
        brandSpinner = findViewById(R.id.brandSpinner)
        sortSpinner = findViewById(R.id.sortSpinner)
        etPriceMin = findViewById(R.id.etPriceMin)
        etPriceMax = findViewById(R.id.etPriceMax)

        val sortOptions = listOf("Default", "Price: Low to High", "Price: High to Low")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateCatalogFilter()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                updateCatalogFilter()
                return true
            }
        })

        etPriceMin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateCatalogFilter() }
        })

        etPriceMax.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateCatalogFilter() }
        })

        // --- Catalog Loading ---
        lifecycleScope.launch {
            try {
                val products = ApiClient.productsApi.getProducts()
                val adapter = ProductListAdapter(products)
                recycler.adapter = adapter

                val uniqueBrands = mutableListOf("All Brands")
                val serverBrands = products.map { it.brand }.distinct().filter { !it.isNullOrEmpty() }
                uniqueBrands.addAll(serverBrands)

                val spinnerAdapter = ArrayAdapter(
                    this@CatalogActivity,
                    android.R.layout.simple_spinner_item,
                    uniqueBrands
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                brandSpinner.adapter = spinnerAdapter

                brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        updateCatalogFilter()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                adapter.applyFilters("", "All Brands", 0.0, Double.MAX_VALUE, "Default")

            } catch (e: Exception) {
                Toast.makeText(this@CatalogActivity, "Catalog Loading Error", Toast.LENGTH_LONG).show()
            }
        }

        // --- Favorites Loading ---
        lifecycleScope.launch {
            if (!FavoriteProductsCache.loadedOnce) {
                try {
                    val favs = ApiClient.favoritesApi.getFavorites()
                    FavoriteProductsCache.setFavorites(favs.map { it.id })
                    FavoriteProductsCache.loadedOnce = true
                } catch (e: Exception) {
                    Toast.makeText(this@CatalogActivity, "Failed to load favorites", Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            FavoriteProductsCache.favorites.collectLatest {
                (recycler.adapter as? ProductListAdapter)?.notifyDataSetChanged()
            }
        }
    }

    private fun updateCatalogFilter() {
        val adapter = recycler.adapter as? ProductListAdapter ?: return

        val query = searchView.query.toString()
        val brand = brandSpinner.selectedItem?.toString() ?: "All Brands"
        val sortType = sortSpinner.selectedItem?.toString() ?: "Default"
        val minPrice = etPriceMin.text.toString().toDoubleOrNull() ?: 0.0
        val maxPrice = etPriceMax.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE

        adapter.applyFilters(query, brand, minPrice, maxPrice, sortType)
    }
}
