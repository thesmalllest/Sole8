package com.example.sole8

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sole8.adapters.ProductListAdapter
import com.example.sole8.network.ApiClient
import com.example.sole8.util.FavoriteProductsCache
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CatalogActivity : BaseDrawerActivity() {

    private lateinit var prefs: UserPreferences
    private lateinit var searchView: SearchView
    private lateinit var recycler: RecyclerView
    private lateinit var brandSpinner: Spinner
    private lateinit var sortSpinner: Spinner
    private lateinit var etPriceMin: EditText
    private lateinit var etPriceMax: EditText

    private val sortKeys = listOf(
        ProductListAdapter.SORT_DEFAULT,
        ProductListAdapter.SORT_PRICE_LOW_HIGH,
        ProductListAdapter.SORT_PRICE_HIGH_LOW
    )

    private val brandKeys = mutableListOf<String>()

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

        setupSortSpinner()
        setupSearchAndPriceFilters()
        loadCatalog()
        loadFavorites()
        observeFavorites()
    }

    private fun setupSortSpinner() {
        val sortOptions = listOf(
            getString(R.string.sort_default),
            getString(R.string.sort_price_low_high),
            getString(R.string.sort_price_high_low)
        )

        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateCatalogFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearchAndPriceFilters() {
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

            override fun afterTextChanged(s: Editable?) {
                updateCatalogFilter()
            }
        })

        etPriceMax.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateCatalogFilter()
            }
        })
    }

    private fun loadCatalog() {
        lifecycleScope.launch {
            try {
                val products = ApiClient.productsApi.getProducts()
                val adapter = ProductListAdapter(products)
                recycler.adapter = adapter

                val serverBrands = products.map { it.brand }.distinct().filter { !it.isNullOrEmpty() }

                brandKeys.clear()
                brandKeys.add(ProductListAdapter.BRAND_ALL)
                brandKeys.addAll(serverBrands)

                val brandTitles = mutableListOf(getString(R.string.filter_all_brands))
                brandTitles.addAll(serverBrands)

                val spinnerAdapter = ArrayAdapter(
                    this@CatalogActivity,
                    android.R.layout.simple_spinner_item,
                    brandTitles
                )

                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                brandSpinner.adapter = spinnerAdapter

                brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        updateCatalogFilter()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                adapter.applyFilters(
                    query = "",
                    brand = ProductListAdapter.BRAND_ALL,
                    minPrice = 0.0,
                    maxPrice = Double.MAX_VALUE,
                    sortType = ProductListAdapter.SORT_DEFAULT
                )

            } catch (e: Exception) {
                Toast.makeText(
                    this@CatalogActivity,
                    getString(R.string.catalog_loading_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            if (!FavoriteProductsCache.loadedOnce) {
                try {
                    val favs = ApiClient.favoritesApi.getFavorites()
                    FavoriteProductsCache.setFavorites(favs.map { it.id })
                    FavoriteProductsCache.loadedOnce = true

                } catch (e: Exception) {
                    Toast.makeText(
                        this@CatalogActivity,
                        getString(R.string.favorites_loading_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun observeFavorites() {
        lifecycleScope.launch {
            FavoriteProductsCache.favorites.collectLatest {
                (recycler.adapter as? ProductListAdapter)?.notifyDataSetChanged()
            }
        }
    }

    private fun updateCatalogFilter() {
        val adapter = recycler.adapter as? ProductListAdapter ?: return

        val query = searchView.query.toString()

        val brandPosition = brandSpinner.selectedItemPosition
        val brand = if (brandPosition in brandKeys.indices) {
            brandKeys[brandPosition]
        } else {
            ProductListAdapter.BRAND_ALL
        }

        val sortPosition = sortSpinner.selectedItemPosition
        val sortType = if (sortPosition in sortKeys.indices) {
            sortKeys[sortPosition]
        } else {
            ProductListAdapter.SORT_DEFAULT
        }

        val minPrice = etPriceMin.text.toString().toDoubleOrNull() ?: 0.0
        val maxPrice = etPriceMax.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE

        adapter.applyFilters(query, brand, minPrice, maxPrice, sortType)
    }
}