package com.example.roomdatabase

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

import android.util.Log

import android.app.Dialog
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class ProductFragment : Fragment() {

    private var rvProducts: RecyclerView? = null
    private var etSearch: EditText? = null
    private var shimmerView: ShimmerFrameLayout? = null
    private var allProducts: List<Product> = listOf()

    private val tag = "ProductFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvProducts = view.findViewById(R.id.rvProducts)
        etSearch = view.findViewById(R.id.etSearch)
        shimmerView = view.findViewById(R.id.shimmerView)

        rvProducts?.layoutManager = GridLayoutManager(requireContext(), 2)

        setupSearch()
        fetchProducts()
    }

    private fun setupSearch() {
        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterProducts(query: String) {
        val filteredList = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter {
                it.title.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) ||
                it.category.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
            }
        }
        rvProducts?.adapter = ProductAdapter(filteredList, { product ->
            showProductDetailsDialog(product)
        }, { product ->
            addToWishlist(product)
        }, { product ->
            addToCart(product)
        })
    }

    private fun addToCart(product: Product) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                withContext(Dispatchers.IO) {
                    val existingItem = db.cartDao().getCartItemById(product.id)
                    if (existingItem != null) {
                        existingItem.quantity += 1
                        db.cartDao().updateCartItem(existingItem)
                    } else {
                        db.cartDao().insertCartItem(CartItem(product))
                    }
                }
                Toast.makeText(requireContext(), "${product.title} added to Cart!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error adding to cart", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addToWishlist(product: Product) {
        Toast.makeText(requireContext(), "${product.title} added to Wishlist!", Toast.LENGTH_SHORT).show()
    }

    private fun fetchProducts() {
        Log.d(tag, "fetchProducts: Started")
        shimmerView?.visibility = View.VISIBLE
        shimmerView?.startShimmer()
        rvProducts?.visibility = View.GONE
        
        val appContext = requireActivity().applicationContext

        lifecycleScope.launch {
            try {
                // 1. First, load from local Room database to show cached data instantly
                val db = AppDatabase.getDatabase(appContext)
                val productDao = db.productDao()
                
                val cachedProducts = withContext(Dispatchers.IO) { productDao.getAllProducts() }
                if (cachedProducts.isNotEmpty()) {
                    allProducts = cachedProducts
                    if (isAdded) {
                        rvProducts?.adapter = ProductAdapter(allProducts, { product ->
                            showProductDetailsDialog(product)
                        }, { product ->
                            addToWishlist(product)
                        }, { product ->
                            addToCart(product)
                        })
                        
                        // Hide shimmer if we have cached data
                        shimmerView?.stopShimmer()
                        shimmerView?.visibility = View.GONE
                        rvProducts?.visibility = View.VISIBLE
                    }
                }

                // 2. Fetch live data from API to refresh the list
                Log.d(tag, "fetchProducts: Calling API")
                val apiService = ApiService.create()
                val remoteProducts = apiService.getProducts()
                
                if (remoteProducts.isNotEmpty()) {
                    Log.d(tag, "fetchProducts: API Success, found ${remoteProducts.size} items")
                    
                    // 3. Update local Room database with fresh data
                    val updatedProducts = withContext(Dispatchers.IO) {
                        productDao.deleteAllProducts()
                        productDao.insertProducts(remoteProducts)
                        productDao.getAllProducts()
                    }
                    
                    // 4. Refresh UI with latest data
                    allProducts = updatedProducts
                    if (isAdded) {
                        rvProducts?.adapter = ProductAdapter(allProducts, { product ->
                            showProductDetailsDialog(product)
                        }, { product ->
                            addToWishlist(product)
                        }, { product ->
                            addToCart(product)
                        })
                    }
                }

            } catch (e: Exception) {
                Log.e(tag, "fetchProducts: Exception: ${e.message}")
                if (allProducts.isEmpty() && isAdded) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                // Final UI Cleanup
                if (isAdded) {
                    shimmerView?.stopShimmer()
                    shimmerView?.visibility = View.GONE
                    rvProducts?.visibility = View.VISIBLE
                    
                    if (allProducts.isEmpty()) {
                        Toast.makeText(requireContext(), "No products available", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d(tag, "fetchProducts: Finished")
            }
        }
    }

    private fun showProductDetailsDialog(product: Product) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_product_details)
        
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }

        val ivImage = dialog.findViewById<ImageView>(R.id.ivProductDetailImage)
        val tvCategory = dialog.findViewById<TextView>(R.id.tvDetailCategory)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvDetailTitle)
        val tvPrice = dialog.findViewById<TextView>(R.id.tvDetailPrice)
        val tvDescription = dialog.findViewById<TextView>(R.id.tvDetailDescription)
        val btnAddToCart = dialog.findViewById<Button>(R.id.btnAddToCartDetail)
        val btnCloseBottom = dialog.findViewById<Button>(R.id.btnCloseDetail)
        val ivWishlist = dialog.findViewById<ImageView>(R.id.ivDetailWishlist)

        tvCategory.text = product.category
        tvTitle.text = product.title
        tvPrice.text = String.format(Locale.getDefault(), "$%.2f", product.price)
        tvDescription.text = product.description

        Glide.with(requireContext())
            .load(product.image)
            .into(ivImage)

        ivWishlist.setOnClickListener {
            Toast.makeText(requireContext(), "${product.title} added to Wishlist!", Toast.LENGTH_SHORT).show()
        }

        btnAddToCart.setOnClickListener {
            addToCart(product)
            dialog.dismiss()
        }

        btnCloseBottom.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Removed syncFromApi as it's now integrated above for better error handling and sequence
}