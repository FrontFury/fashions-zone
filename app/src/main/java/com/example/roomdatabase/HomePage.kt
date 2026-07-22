package com.example.roomdatabase

import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class HomePage : AppCompatActivity() {

    private lateinit var navHome: TextView
    private lateinit var navProduct: TextView
    private lateinit var navCart: TextView
    private lateinit var navProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        val mainView = findViewById<android.view.View>(R.id.fragmentContainer)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        navHome = findViewById(R.id.navHome)
        navProduct = findViewById(R.id.navProduct)
        navCart = findViewById(R.id.navCart)
        navProfile = findViewById(R.id.navProfile)

        // Set initial fragment
        loadFragment(HomeFragment(), navHome)

        navHome.setOnClickListener { loadFragment(HomeFragment(), navHome) }
        navProduct.setOnClickListener { loadFragment(ProductFragment(), navProduct) }
        navCart.setOnClickListener { loadFragment(CartFragment(), navCart) }
        navProfile.setOnClickListener { loadFragment(ProfileFragment(), navProfile) }
    }

    private fun loadFragment(fragment: Fragment, activeTab: TextView) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
        updateNavUI(activeTab)
    }

    private fun updateNavUI(activeTab: TextView) {
        val tabs = listOf(navHome, navProduct, navCart, navProfile)
        
        for (tab in tabs) {
            if (tab == activeTab) {
                tab.setBackgroundResource(R.drawable.nav_item_active)
                tab.setTextColor(ContextCompat.getColor(this, R.color.white))
                tab.setTypeface(null, Typeface.BOLD)
            } else {
                tab.setBackgroundResource(R.drawable.nav_item_inactive)
                tab.setTextColor(ContextCompat.getColor(this, R.color.primary_gold))
                tab.setTypeface(null, Typeface.NORMAL)
            }
        }
    }
}