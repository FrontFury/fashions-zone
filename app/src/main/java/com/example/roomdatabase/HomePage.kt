package com.example.roomdatabase

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class HomePage : AppCompatActivity() {

    private lateinit var navHomeContainer: LinearLayout
    private lateinit var navProductContainer: LinearLayout
    private lateinit var navCartContainer: LinearLayout
    private lateinit var navProfileContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        val mainView = findViewById<View>(R.id.fragmentContainer)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        navHomeContainer = findViewById(R.id.navHomeContainer)
        navProductContainer = findViewById(R.id.navProductContainer)
        navCartContainer = findViewById(R.id.navCartContainer)
        navProfileContainer = findViewById(R.id.navProfileContainer)

        // Set initial fragment
        loadFragment(HomeFragment(), navHomeContainer)

        navHomeContainer.setOnClickListener { loadFragment(HomeFragment(), navHomeContainer) }
        navProductContainer.setOnClickListener { loadFragment(ProductFragment(), navProductContainer) }
        navCartContainer.setOnClickListener { loadFragment(CartFragment(), navCartContainer) }
        navProfileContainer.setOnClickListener { loadFragment(ProfileFragment(), navProfileContainer) }
    }

    private fun loadFragment(fragment: Fragment, activeTab: LinearLayout) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
        updateNavUI(activeTab)
    }

    private fun updateNavUI(activeTab: LinearLayout) {
        val containers = listOf(navHomeContainer, navProductContainer, navCartContainer, navProfileContainer)
        
        for (container in containers) {
            val icon = container.getChildAt(0) as ImageView
            val label = container.getChildAt(1) as TextView
            
            if (container == activeTab) {
                // Active state: Primary gold color
                icon.setColorFilter(ContextCompat.getColor(this, R.color.primary_gold))
                label.setTextColor(ContextCompat.getColor(this, R.color.primary_gold))
                label.alpha = 1.0f
            } else {
                // Inactive state: Lighter gold color and semi-transparent
                icon.setColorFilter(ContextCompat.getColor(this, R.color.gold_light))
                label.setTextColor(ContextCompat.getColor(this, R.color.gold_light))
                label.alpha = 0.6f
            }
        }
    }
}
