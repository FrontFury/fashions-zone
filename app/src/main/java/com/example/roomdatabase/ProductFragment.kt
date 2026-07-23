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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductFragment : Fragment() {

    private var rvProducts: RecyclerView? = null
    private var etSearch: EditText? = null
    private var shimmerView: ShimmerFrameLayout? = null
    private var fabAddProduct: FloatingActionButton? = null
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
        fabAddProduct = view.findViewById(R.id.fabAddProduct)

        rvProducts?.layoutManager = GridLayoutManager(requireContext(), 2)

        fabAddProduct?.setOnClickListener {
            showAddProductDialog()
        }

        setupSearch()
        fetchProducts()
    }

    private fun showAddProductDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_product)
        
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }

        val etTitle = dialog.findViewById<EditText>(R.id.etProductTitle)
        val etPrice = dialog.findViewById<EditText>(R.id.etProductPrice)
        val etCategory = dialog.findViewById<EditText>(R.id.etProductCategory)
        val etDescription = dialog.findViewById<EditText>(R.id.etProductDescription)
        val etImage = dialog.findViewById<EditText>(R.id.etProductImage)
        val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmit)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString()
            val priceStr = etPrice.text.toString()
            val category = etCategory.text.toString()
            val description = etDescription.text.toString()
            val image = etImage.text.toString()

            if (title.isNotEmpty()) {
                val price = priceStr.toDoubleOrNull() ?: 0.0
                // Use id = 0 for auto-generation
                val newProduct = Product(id = 0, title = title, price = price, description = description, category = category, image = image)
                saveCustomProduct(newProduct)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun saveCustomProduct(product: Product) {
        Log.d(tag, "saveCustomProduct: Saving ${product.title}")
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(requireContext())
                    db.productDao().insertProducts(listOf(product))
                    Log.d(tag, "saveCustomProduct: Insert successful")
                }
                Toast.makeText(requireContext(), "Product added successfully!", Toast.LENGTH_SHORT).show()
                fetchProducts() // Refresh the list from local DB
            } catch (e: Exception) {
                Log.e(tag, "saveCustomProduct Error: ${e.message}")
                Toast.makeText(requireContext(), "Error saving product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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
        updateRecyclerView(filteredList)
    }

    private fun updateRecyclerView(products: List<Product>) {
        if (!isAdded) return
        rvProducts?.adapter = ProductAdapter(products, { product ->
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
        
        if (allProducts.isEmpty()) {
            shimmerView?.visibility = View.VISIBLE
            shimmerView?.startShimmer()
            rvProducts?.visibility = View.GONE
        }
        
        val appContext = requireActivity().applicationContext

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(appContext)
                val productDao = db.productDao()
                
                // 1. Load from local Room database first
                val cachedProducts = withContext(Dispatchers.IO) { 
                    val list = productDao.getAllProducts()
                    Log.d(tag, "fetchProducts: Loaded ${list.size} from local DB")
                    list
                }
                if (cachedProducts.isNotEmpty()) {
                    allProducts = cachedProducts
                    updateRecyclerView(allProducts)
                    
                    shimmerView?.stopShimmer()
                    shimmerView?.visibility = View.GONE
                    rvProducts?.visibility = View.VISIBLE
                }

                // 2. Fetch live data from API
                try {
                    Log.d(tag, "fetchProducts: Calling API")
                    val apiService = ApiService.create()
                    val remoteProducts = apiService.getProducts()
                    
                    if (remoteProducts.isNotEmpty()) {
                        Log.d(tag, "fetchProducts: API Success, found ${remoteProducts.size} items")
                        
                        // 3. Save to Room
                        withContext(Dispatchers.IO) {
                            productDao.insertProducts(remoteProducts)
                            Log.d(tag, "fetchProducts: API products saved to Room")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "fetchProducts API Error: ${e.message}")
                }

                // 4. Reload from DB to get the combined list (API + Manual)
                val finalProducts = withContext(Dispatchers.IO) { 
                    val list = productDao.getAllProducts()
                    Log.d(tag, "fetchProducts: Final count from DB: ${list.size}")
                    list
                }
                allProducts = finalProducts
                updateRecyclerView(allProducts)

            } catch (e: Exception) {
                Log.e(tag, "fetchProducts Fatal Error: ${e.message}")
                if (allProducts.isEmpty() && isAdded) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                if (isAdded) {
                    shimmerView?.stopShimmer()
                    shimmerView?.visibility = View.GONE
                    rvProducts?.visibility = View.VISIBLE
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
}