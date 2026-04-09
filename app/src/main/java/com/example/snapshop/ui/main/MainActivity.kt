package com.example.snapshop.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.snapshop.R
import com.example.snapshop.databinding.ActivityMainBinding
import com.example.snapshop.ui.auth.AuthViewModel
import com.example.snapshop.ui.auth.LoginActivity
import com.example.snapshop.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController


        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    // Pop everything back to home cleanly, never create a duplicate
                    navController.popBackStack(R.id.homeFragment, false)
                    true
                }
                R.id.favoritesFragment -> {
                    navController.navigate(R.id.favoritesFragment)
                    true
                }
                else -> false
            }
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment ->
                    binding.bottomNavigationView.menu.findItem(R.id.homeFragment)?.isChecked = true
                R.id.favoritesFragment ->
                    binding.bottomNavigationView.menu.findItem(R.id.favoritesFragment)?.isChecked = true
            }

            when (destination.id) {
                R.id.homeFragment, R.id.favoritesFragment -> {
                    binding.topBar.visibility = View.VISIBLE
                    binding.btnOpenDrawer.visibility = View.VISIBLE
                    binding.tvAppTitle.visibility = View.VISIBLE
                }
                else -> {
                    binding.topBar.visibility = View.GONE
                    binding.btnOpenDrawer.visibility = View.GONE
                    binding.tvAppTitle.visibility = View.GONE
                }
            }
        }

        // Drawer setup
        drawerLayout = binding.drawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout,
            R.string.nav_open, R.string.nav_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Drawer item clicks
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home        -> navController.popBackStack(R.id.homeFragment, false)
                R.id.nav_favourites  -> navController.navigate(R.id.favoritesFragment)
                R.id.nav_add_product -> navController.navigate(R.id.uploadFragment)
                R.id.nav_profile     -> { /* navigate to profile when ready */ }
                R.id.nav_about       -> { /* navigate to about when ready */ }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val header = binding.navigationView.getHeaderView(0)

        // Logout button
        header.findViewById<android.widget.Button>(R.id.logoutButton).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupNavDrawerHeader(header)
    }


    private fun setupNavDrawerHeader(header: android.view.View) {
        val ivPhoto = header.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profileImage)
        val tvName  = header.findViewById<TextView>(R.id.profileName)
        val tvEmail = header.findViewById<TextView>(R.id.profileEmail)

        authViewModel.userProfile.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val user = resource.data
                    tvName.text  = user.name
                    tvEmail.text = user.email

                    Glide.with(this)
                        .load(user.photoUrl.ifEmpty { null })
                        .placeholder(R.drawable.ic_default_avtar)
                        .error(R.drawable.ic_default_avtar)
                        .circleCrop()
                        .into(ivPhoto)
                }
                else -> Unit
            }
        }

        authViewModel.loadUserProfile()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}