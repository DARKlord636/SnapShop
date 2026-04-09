package com.example.snapshop.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.snapshop.data.model.Product
import com.example.snapshop.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,           // ✅ Firebase Storage injected
    @ApplicationContext private val context: Context
) {


    private suspend fun uploadImageToFirebase(uri: Uri): String {
        val fileName = "product_images/${UUID.randomUUID()}.jpg"
        val imageRef = storage.reference.child(fileName)

        Log.d("FIREBASE_STORAGE", "Uploading image: $uri")
        imageRef.putFile(uri).await()

        val downloadUrl = imageRef.downloadUrl.await().toString()
        Log.d("FIREBASE_STORAGE", "Upload success! URL: $downloadUrl")

        return downloadUrl
    }


    suspend fun uploadProduct(product: Product, imageUris: List<Uri>): Resource<String> {
        return try {
            Log.d("FIREBASE_STORAGE", "Uploading ${imageUris.size} images...")
            val imageUrls = mutableListOf<String>()

            for ((index, uri) in imageUris.withIndex()) {
                Log.d("FIREBASE_STORAGE", "Uploading image ${index + 1} of ${imageUris.size}")
                val url = uploadImageToFirebase(uri)
                imageUrls.add(url)
            }

            Log.d("FIREBASE_STORAGE", "All images uploaded. Saving to Firestore...")
            val productId = UUID.randomUUID().toString()
            val productWithImages = product.copy(
                productId = productId,
                imageUrls = imageUrls
            )

            firestore.collection("products")
                .document(productId)
                .set(productWithImages)
                .await()

            Log.d("FIREBASE_STORAGE", "Product saved to Firestore successfully!")
            Resource.Success(productId)

        } catch (e: Exception) {
            Log.e("FIREBASE_STORAGE", "uploadProduct failed: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to upload product")
        }
    }


    suspend fun getAllProducts(): Resource<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            val products = snapshot.toObjects(Product::class.java)
            Resource.Success(products)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch products")
        }
    }


    suspend fun getProductById(productId: String): Resource<Product> {
        return try {
            val document = firestore.collection("products")
                .document(productId)
                .get()
                .await()
            val product = document.toObject(Product::class.java)
            if (product != null) Resource.Success(product)
            else Resource.Error("Product not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch product")
        }
    }


    suspend fun getUserProducts(userId: String): Resource<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("uploaderId", userId)
                .get()
                .await()
            val products = snapshot.toObjects(Product::class.java)
            Resource.Success(products)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch user products")
        }
    }
}