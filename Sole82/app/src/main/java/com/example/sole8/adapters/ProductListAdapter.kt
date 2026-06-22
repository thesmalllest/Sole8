package com.example.sole8.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sole8.R
import com.example.sole8.models.api.ProductListDto
import com.example.sole8.ProductDetailsActivity
import com.example.sole8.util.FavoriteProductsCache

class ProductListAdapter(private val itemsAll: List<ProductListDto>) :
    RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {

    private var itemsFull: List<ProductListDto> = itemsAll

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.catalogImage)
        val name: TextView = view.findViewById(R.id.catalogName)
        val price: TextView = view.findViewById(R.id.catalogPrice)
        val favIcon: ImageView = view.findViewById(R.id.favIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_catalog, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = itemsFull[position]

        Glide.with(holder.itemView.context)
            .load(product.thumbnailUrl)
            .placeholder(R.drawable.loading)
            .error(R.drawable.error_image)
            .into(holder.image)

        holder.name.text = product.name
        holder.price.text = "${product.price} ₽"

        val isFav = FavoriteProductsCache.isFavorite(product.id)
        holder.favIcon.setImageResource(
            if (isFav) R.drawable.ic_favorite
            else R.drawable.ic_favorite_border
        )

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductDetailsActivity::class.java)
            intent.putExtra("productId", product.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = itemsFull.size

    fun applyFilters(query: String, brand: String, minPrice: Double, maxPrice: Double, sortType: String) {
        var filteredList = itemsAll.filter { product ->
            val matchesQuery = query.isEmpty() || product.name.contains(query, ignoreCase = true)
            val matchesBrand = brand == "All Brands" || product.brand.equals(brand, ignoreCase = true)
            val productPrice = product.price.toDouble()
            val matchesPrice = productPrice in minPrice..maxPrice

            matchesQuery && matchesBrand && matchesPrice
        }

        filteredList = when (sortType) {
            "Price: Low to High" -> filteredList.sortedBy { it.price }
            "Price: High to Low" -> filteredList.sortedByDescending { it.price }
            else -> filteredList
        }

        itemsFull = filteredList
        notifyDataSetChanged()
    }
}
