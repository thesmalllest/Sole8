package com.example.sole8.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sole8.ProductDetailsActivity
import com.example.sole8.R
import com.example.sole8.models.api.OrderItemDetails

class OrderProductsAdapter(private val items: List<OrderItemDetails>) :
    RecyclerView.Adapter<OrderProductsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.ivOrderProductImg)
        val name: TextView = view.findViewById(R.id.tvOrderProductName)
        val price: TextView = view.findViewById(R.id.tvOrderProductPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.name.text = item.productName
        holder.price.text = context.getString(R.string.order_product_price_format, item.price.toInt())

        val displayImage = if (!item.imageUrl.isNullOrEmpty()) {
            item.imageUrl
        } else {
            "android.resource://" + context.packageName + "/" + R.drawable.logo
        }

        Glide.with(context)
            .load(displayImage)
            .placeholder(R.drawable.loading)
            .error(R.drawable.error_image)
            .into(holder.img)

        holder.img.isClickable = true
        holder.img.isFocusable = true

        holder.img.setOnClickListener {
            val intent = Intent(context, ProductDetailsActivity::class.java)
            intent.putExtra("productId", item.productId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size
}