package com.example.roomdatabase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CartFragment : Fragment() {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvBottomTotal: TextView
    private lateinit var tvItemCount: TextView
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)
        
        rvCart = view.findViewById(R.id.rvCart)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotal = view.findViewById(R.id.tvTotal)
        tvBottomTotal = view.findViewById(R.id.tvBottomTotal)
        tvItemCount = view.findViewById(R.id.tvItemCount)
        
        rvCart.layoutManager = LinearLayoutManager(requireContext())
        adapter = CartAdapter(emptyList()) { item, newQuantity ->
            updateQuantity(item, newQuantity)
        }
        rvCart.adapter = adapter
        
        loadCartItems()
        
        return view
    }

    private fun loadCartItems() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val items = withContext(Dispatchers.IO) { db.cartDao().getAllCartItems() }
            
            adapter.updateData(items)
            updateSummary(items)
        }
    }

    private fun updateQuantity(item: CartItem, newQuantity: Int) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            withContext(Dispatchers.IO) {
                if (newQuantity > 0) {
                    item.quantity = newQuantity
                    db.cartDao().updateCartItem(item)
                } else {
                    db.cartDao().deleteCartItem(item)
                }
            }
            loadCartItems()
        }
    }

    private fun updateSummary(items: List<CartItem>) {
        var subtotal = 0.0
        var count = 0
        for (item in items) {
            subtotal += item.price * item.quantity
            count += item.quantity
        }
        
        val tax = subtotal * 0.08 // 8% tax
        val total = subtotal + tax // Shipping is FREE
        
        tvSubtotal.text = String.format(Locale.getDefault(), "$%.2f", subtotal)
        tvTax.text = String.format(Locale.getDefault(), "$%.2f", tax)
        tvTotal.text = String.format(Locale.getDefault(), "$%.2f", total)
        tvBottomTotal.text = String.format(Locale.getDefault(), "$%.2f", total)
        tvItemCount.text = "$count Items"
    }
}