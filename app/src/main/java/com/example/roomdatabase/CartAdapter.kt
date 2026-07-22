package com.example.roomdatabase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val onQuantityChanged: (CartItem, Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCartImage: ImageView = view.findViewById(R.id.ivCartImage)
        val tvCartTitle: TextView = view.findViewById(R.id.tvCartTitle)
        val tvCartPrice: TextView = view.findViewById(R.id.tvCartPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val btnPlus: View = view.findViewById(R.id.btnPlus)
        val btnMinus: View = view.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        
        holder.tvCartTitle.text = item.title
        holder.tvCartPrice.text = String.format(Locale.getDefault(), "$%.2f", item.price)
        holder.tvQuantity.text = item.quantity.toString()

        Glide.with(holder.ivCartImage.context)
            .load(item.image)
            .into(holder.ivCartImage)

        holder.btnPlus.setOnClickListener {
            onQuantityChanged(item, item.quantity + 1)
        }

        holder.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                onQuantityChanged(item, item.quantity - 1)
            } else {
                onQuantityChanged(item, 0) // Removal handled by Fragment
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateData(newList: List<CartItem>) {
        cartItems = newList
        notifyDataSetChanged()
    }
}