package com.example.sole8.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sole8.ProductDetailsActivity
import com.example.sole8.R
import com.example.sole8.models.api.ProductListDto

class ProductAdapter(private val items: List<ProductListDto>) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.productImage)
        val name: TextView = view.findViewById(R.id.productName)
        val price: TextView = view.findViewById(R.id.productPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = items[position]
        val context = holder.itemView.context

        Log.d("IMAGE_DEBUG", "Loading URL: ${product.thumbnailUrl}")

        Glide.with(context)
            .load(product.thumbnailUrl)
            .placeholder(R.drawable.loading)
            .error(R.drawable.error_image)
            .into(holder.image)

        holder.name.text = product.name
        holder.price.text = context.getString(R.string.product_price_format, product.price.toInt())

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductDetailsActivity::class.java)

            intent.putExtra("productId", product.id)
            intent.putExtra("name", product.name)
            intent.putExtra("price", product.price.toString())
            intent.putExtra("image", product.thumbnailUrl)
            intent.putExtra("model3DUrl", product.model3DUrl)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size
}