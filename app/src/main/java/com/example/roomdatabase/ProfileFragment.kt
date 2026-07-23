package com.example.roomdatabase

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var ivProfileImage: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var switchDarkMode: SwitchMaterial

    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            imageUri?.let {
                uploadImageToFirebase(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserPhone = view.findViewById(R.id.tvUserPhone)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        switchDarkMode = view.findViewById(R.id.switchDarkMode)

        val btnLogout = view.findViewById<View>(R.id.btnLogout)
        val btnProfileDetails = view.findViewById<View>(R.id.btnProfileDetails)
        val btnSettings = view.findViewById<View>(R.id.btnSettings)

        loadUserData()

        ivProfileImage.setOnClickListener {
            openImagePicker()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btnProfileDetails.setOnClickListener {
            Toast.makeText(context, "Profile Details clicked", Toast.LENGTH_SHORT).show()
        }

        btnSettings.setOnClickListener {
            Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT).show()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        return view
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded && snapshot.exists()) {
                    val name = snapshot.child("name").value?.toString() ?: ""
                    val phone = snapshot.child("phone").value?.toString() ?: ""
                    val email = snapshot.child("email").value?.toString() ?: ""
                    val profileImageUrl = snapshot.child("profileImageUrl").value?.toString() ?: ""

                    tvUserName.text = if (name.isNotEmpty()) name else getString(R.string.user_name_default)
                    tvUserPhone.text = if (phone.isNotEmpty()) phone else "+0000-000-0000"
                    tvUserEmail.text = if (email.isNotEmpty()) email else auth.currentUser?.email

                    if (profileImageUrl.isNotEmpty() && profileImageUrl != "null") {
                        Glide.with(this@ProfileFragment)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.profilelogo)
                            .circleCrop()
                            .into(ivProfileImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val storageRef = storage.getReference("profile_images").child("$uid.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveProfileImageUrlToDatabase(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileImageUrlToDatabase(url: String) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(uid)

        userRef.child("profileImageUrl").setValue(url)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile image updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update database", Toast.LENGTH_SHORT).show()
            }
    }
}
