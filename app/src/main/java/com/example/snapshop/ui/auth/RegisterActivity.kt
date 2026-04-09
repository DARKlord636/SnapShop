package com.example.snapshop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.snapshop.R
import com.example.snapshop.databinding.ActivityRegisterBinding
import com.example.snapshop.ui.main.MainActivity
import com.example.snapshop.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                authViewModel.signUpWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-Up failed: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignIn()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.trim().matches(Regex("^[0-9]{10}$"))
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name     = binding.etName.text.toString().trim()
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm  = binding.etConfirmPassword.text.toString().trim()
            val contact  = binding.etContact.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() ||
                password.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidPhoneNumber(contact)) {
                Toast.makeText(this, "enter valid phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val (isEmailValid, emailError) = authViewModel.validateEmail(email)
            if (!isEmailValid) {
                binding.etEmail.error = emailError
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.registerUser(name, email, password, contact)
        }

        binding.btnGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun observeViewModel() {

        authViewModel.registerState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(this,
                        "✅ Account created successfully!", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({ goToMain() }, 1500)
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        authViewModel.googleSignInState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(this,
                        "✅ Signed up with Google!", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({ goToMain() }, 1500)
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled  = !isLoading
        binding.btnGoogle.isEnabled    = !isLoading
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}