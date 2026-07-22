package com.example.roomdatabase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale

class ProductAdapter(private val products: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProductImage: ImageView = view.findViewById(R.id.ivProductImage)
        val tvProductCategory: TextView = view.findViewById(R.id.tvProductCategory)
        val tvProductTitle: TextView = view.findViewById(R.id.tvProductTitle)
        val tvProductPrice: TextView = view.findViewById(R.id.tvProductPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        
        holder.tvProductTitle.text = product.title
        holder.tvProductCategory.text = product.category
        holder.tvProductPrice.text = String.format(Locale.getDefault(), "$%.2f", product.price)

        Glide.with(holder.ivProductImage.context)
            .load(product.image)
            .into(holder.ivProductImage)
    }

    override fun getItemCount(): Int = products.size
}