package com.example.snapshop.data.repository


import com.example.snapshop.data.model.User
import com.example.snapshop.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser
    val isLoggedIn: Boolean get() = firebaseAuth.currentUser != null




    fun isEmailValid(email: String): Pair<Boolean, String> {
        val trimmed = email.trim().lowercase()

        // 1. Basic RFC-like format check
        val emailRegex = Regex(
            "^[a-z0-9][a-z0-9._\\-]{1,28}[a-z0-9]@[a-z0-9.-]+\\.[a-z]{2,}$"
        )
        if (!emailRegex.matches(trimmed)) {
            return Pair(false, "Enter a valid email address")
        }


        val allowedDomains = setOf(
            "gmail.com", "yahoo.com", "yahoo.in", "yahoo.co.in",
            "outlook.com", "hotmail.com", "live.com",
            "icloud.com", "me.com", "mac.com",
            "protonmail.com", "proton.me",
            "rediffmail.com", "aol.com",
            "zoho.com", "ymail.com"
        )
        val domain = trimmed.substringAfter("@")
        if (domain !in allowedDomains) {
            return Pair(false, "Please use a genuine email provider (Gmail, Yahoo, Outlook…)")
        }


        val localPart = trimmed.substringBefore("@")
        val letters = localPart.filter { it.isLetter() }
        if (letters.length < 3) {
            return Pair(false, "Email address looks invalid. Please use a real email ID")
        }

        val consonantRunRegex = Regex("[bcdfghjklmnpqrstvwxyz]{5,}")
        if (consonantRunRegex.containsMatchIn(localPart)) {
            return Pair(false, "Email address looks invalid. Please use a real email ID")
        }

        return Pair(true, "")
    }

    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        contact: String
    ): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!

            val user = User(
                userId = firebaseUser.uid,
                name = name,
                email = email,
                contact = contact,
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            Resource.Success(firebaseUser)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }


    suspend fun loginUser(
        email: String,
        password: String
    ): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Resource<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user!!


            val document = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (!document.exists()) {

                firebaseAuth.signOut()
                return Resource.Error("No account found for this Google ID. Please register first.")
            }


            Resource.Success(firebaseUser)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Google Sign-In failed")
        }
    }


    suspend fun signUpWithGoogle(account: GoogleSignInAccount): Resource<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val isNewUser = result.additionalUserInfo?.isNewUser ?: false

            if (!isNewUser) {

                firebaseAuth.signOut()
                return Resource.Error("An account already exists with this Google ID. Please sign in instead.")
            }

            val firebaseUser = result.user!!
            val user = User(
                userId = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                contact = "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            Resource.Success(firebaseUser)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Google Sign-Up failed")
        }
    }



    fun logoutUser() {
        firebaseAuth.signOut()
    }



    suspend fun getUserProfile(userId: String): Resource<User> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val user = document.toObject(User::class.java)
            if (user != null) Resource.Success(user)
            else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch profile")
        }
    }
}