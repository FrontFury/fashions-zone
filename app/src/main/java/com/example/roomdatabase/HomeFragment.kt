package com.example.roomdatabase

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("USER_NAME", "User")
        
        val tvWelcomeHome = view.findViewById<TextView>(R.id.tvWelcomeHome)
        tvWelcomeHome.text = getString(R.string.welcome_home, userName)
        
        return view
    }
}