package com.example.nutricook.data.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.nutricook.view.recipes.CookingStep
import com.example.nutricook.view.recipes.IngredientItem
import com.example.nutricook.utils.NutritionData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class UserRecipeRepository @Inject constructor(
    private val mediaManager: MediaManager,
    @ApplicationContext private val context: Context
) {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Lưu recipe vào Firestore
     */
    suspend fun saveRecipe(
        recipeName: String,
        estimatedTime: String,
        servings: String,
        imageUris: List<Uri>,
        ingredients: List<IngredientItem>,
        cookingSteps: List<CookingStep>,
        description: String,
        notes: String,
        tips: String,
        nutritionData: NutritionData
    ): Result<String> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Bạn chưa đăng nhập"))
            
            // Upload images to Storage - đảm bảo ảnh đầu tiên được upload trước
            val imageUrls = mutableListOf<String>()
            imageUris.forEachIndexed { index, uri ->
                try {
                    android.util.Log.d("UserRecipeRepo", "Uploading image $index of ${imageUris.size}")
                    val imageUrl = uploadImage(uri)
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl)
                        android.util.Log.d("UserRecipeRepo", "Successfully uploaded image $index: $imageUrl")
                    } else {
                        android.util.Log.w("UserRecipeRepo", "Failed to upload image $index")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("UserRecipeRepo", "Error uploading image $index: ${e.message}", e)
                    // Continue with other images even if one fails
                }
            }
            
            // Đảm bảo ảnh đầu tiên luôn ở vị trí đầu tiên trong danh sách
            android.util.Log.d("UserRecipeRepo", "Total images uploaded: ${imageUrls.size} out of ${imageUris.size}")
            if (imageUrls.isEmpty() && imageUris.isNotEmpty()) {
                android.util.Log.w("UserRecipeRepo", "WARNING: No images were uploaded successfully!")
            }
            
            // Convert ingredients to map
            val ingredientsData = ingredients.map { ingredient ->
                mapOf(
                    "name" to ingredient.name,
                    "quantity" to ingredient.quantity,
                    "unit" to ingredient.unit.abbreviation,
                    "foodItemId" to (ingredient.foodItemId ?: ""),
                    "categoryId" to (ingredient.categoryId ?: "")
                )
            }
            
            // Convert cooking steps to map
            val stepsData = cookingSteps.mapIndexed { index, step ->
                val stepImageUrl = try {
                    step.imageUri?.let { uploadImage(it) } ?: ""
                } catch (e: Exception) {
                    android.util.Log.e("UserRecipeRepo", "Error uploading step $index image: ${e.message}", e)
                    ""
                }
                mapOf(
                    "stepNumber" to (index + 1),
                    "description" to step.description,
                    "imageUrl" to stepImageUrl
                )
            }
            
            // Create recipe document
            val recipeData = hashMapOf(
                "recipeName" to recipeName,
                "estimatedTime" to estimatedTime,
                "servings" to servings,
                "imageUrls" to imageUrls,
                "ingredients" to ingredientsData,
                "cookingSteps" to stepsData,
                "description" to description,
                "notes" to notes,
                "tips" to tips,
                "nutritionData" to mapOf(
                    "calories" to nutritionData.calories,
                    "fat" to nutritionData.fat,
                    "carbs" to nutritionData.carbs,
                    "protein" to nutritionData.protein,
                    "cholesterol" to nutritionData.cholesterol,
                    "sodium" to nutritionData.sodium,
                    "vitamin" to nutritionData.vitamin,
                    "vitaminDetails" to mapOf(
                        "vitaminA" to nutritionData.vitaminDetails.vitaminA,
                        "vitaminB1" to nutritionData.vitaminDetails.vitaminB1,
                        "vitaminB2" to nutritionData.vitaminDetails.vitaminB2,
                        "vitaminB3" to nutritionData.vitaminDetails.vitaminB3,
                        "vitaminB6" to nutritionData.vitaminDetails.vitaminB6,
                        "vitaminB9" to nutritionData.vitaminDetails.vitaminB9,
                        "vitaminB12" to nutritionData.vitaminDetails.vitaminB12,
                        "vitaminC" to nutritionData.vitaminDetails.vitaminC,
                        "vitaminD" to nutritionData.vitaminDetails.vitaminD,
                        "vitaminE" to nutritionData.vitaminDetails.vitaminE,
                        "vitaminK" to nutritionData.vitaminDetails.vitaminK
                    )
                ),
                "userId" to currentUser.uid,
                "userEmail" to (currentUser.email ?: ""),
                "createdAt" to com.google.firebase.Timestamp.now(),
                "rating" to 0.0,
                "reviewCount" to 0
            )
            
            // Save to Firestore (even if image upload failed)
            val docRef = firestore.collection("userRecipes").document()
            docRef.set(recipeData).await()
            
            android.util.Log.d("UserRecipeRepo", "Recipe saved successfully with ID: ${docRef.id}")
            
            // Warn if no images were uploaded
            if (imageUrls.isEmpty() && imageUris.isNotEmpty()) {
                android.util.Log.w("UserRecipeRepo", "Recipe saved but no images were uploaded. Check Cloudinary configuration.")
            }
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            android.util.Log.e("UserRecipeRepo", "Error saving recipe to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Upload image to Cloudinary
     */
    private suspend fun uploadImage(uri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    android.util.Log.e("UserRecipeRepo", "User not logged in")
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }
                
                // Generate unique public_id for Cloudinary
                val timestamp = System.currentTimeMillis()
                val random = (0..9999).random()
                val publicId = "recipes/${currentUser.uid}/${timestamp}_${random}"
                
                android.util.Log.d("UserRecipeRepo", "Uploading image to Cloudinary: $publicId")
                
                // Upload to Cloudinary
                val requestId = mediaManager.upload(uri)
                    .option("public_id", publicId)
                    .option("folder", "nutricook/recipes")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            android.util.Log.d("UserRecipeRepo", "Upload started: $requestId")
                        }
                        
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100 / totalBytes).toInt()
                            android.util.Log.d("UserRecipeRepo", "Upload progress: $progress%")
                        }
                        
                        override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                            val secureUrl = resultData["secure_url"] as? String
                            val url = resultData["url"] as? String
                            val imageUrl = secureUrl ?: url
                            
                            if (imageUrl != null) {
                                android.util.Log.d("UserRecipeRepo", "Image uploaded successfully: $imageUrl")
                                continuation.resume(imageUrl)
                            } else {
                                android.util.Log.e("UserRecipeRepo", "Upload succeeded but no URL returned")
                                continuation.resume(null)
                            }
                        }
                        
                        override fun onError(requestId: String, error: ErrorInfo) {
                            android.util.Log.e("UserRecipeRepo", "Upload error: ${error.description}")
                            continuation.resume(null)
                        }
                        
                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            android.util.Log.w("UserRecipeRepo", "Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch(context)
                
                // Note: Cloudinary Android SDK doesn't have a cancel() method
                // Upload will continue even if coroutine is cancelled
                // The callback will still fire when upload completes
            } catch (e: Exception) {
                android.util.Log.e("UserRecipeRepo", "Error starting upload: ${e.message}", e)
                continuation.resume(null)
            }
        }
    }
    
    /**
     * Lấy danh sách recipes của user
     */
    suspend fun getUserRecipes(userId: String): List<Map<String, Any>> {
        return try {
            android.util.Log.d("UserRecipeRepo", "Getting recipes for userId: $userId")
            
            // Thử query với orderBy trước, nếu lỗi thì query không orderBy
            val snapshot = try {
                firestore.collection("userRecipes")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
            } catch (e: Exception) {
                // Nếu lỗi do thiếu composite index, thử query không orderBy
                android.util.Log.w("UserRecipeRepo", "Query with orderBy failed, trying without orderBy: ${e.message}")
                firestore.collection("userRecipes")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
            }
            
            val recipes = snapshot.documents.mapNotNull { doc ->
                val data = doc.data?.toMutableMap()
                if (data != null) {
                    data["docId"] = doc.id // Thêm docId để có thể navigate
                    // Log để debug
                    val recipeName = data["recipeName"] as? String ?: "Unknown"
                    val recipeUserId = data["userId"] as? String ?: "No userId"
                    android.util.Log.d("UserRecipeRepo", "Found recipe: $recipeName, userId: $recipeUserId")
                    data
                } else {
                    null
                }
            }
            
            // Sort manually nếu query không có orderBy
            val sortedRecipes = recipes.sortedByDescending { recipe ->
                val createdAt = recipe["createdAt"]
                when (createdAt) {
                    is com.google.firebase.Timestamp -> createdAt.toDate().time
                    is Long -> createdAt
                    else -> 0L
                }
            }
            
            android.util.Log.d("UserRecipeRepo", "Total recipes found: ${sortedRecipes.size}")
            sortedRecipes
        } catch (e: Exception) {
            android.util.Log.e("UserRecipeRepo", "Error getting user recipes: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Lấy tất cả recipes (cho trang công thức)
     */
    suspend fun getAllRecipes(): List<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection("userRecipes")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Cập nhật recipe vào Firestore
     */
    suspend fun updateRecipe(
        recipeId: String,
        recipeName: String,
        estimatedTime: String,
        servings: String,
        imageUris: List<Uri>,
        ingredients: List<IngredientItem>,
        cookingSteps: List<CookingStep>,
        description: String,
        notes: String,
        tips: String,
        nutritionData: NutritionData,
        existingImageUrls: List<String> = emptyList() // Ảnh cũ nếu không thay đổi
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Bạn chưa đăng nhập"))
            
            // Kiểm tra quyền sở hữu
            val doc = firestore.collection("userRecipes")
                .document(recipeId)
                .get()
                .await()
            
            if (!doc.exists()) {
                return Result.failure(Exception("Không tìm thấy công thức"))
            }
            
            val recipeUserId = doc.data?.get("userId") as? String
            if (recipeUserId != currentUser.uid) {
                return Result.failure(Exception("Bạn không có quyền chỉnh sửa công thức này"))
            }
            
            // Xử lý ảnh: upload ảnh mới nếu có, giữ lại ảnh cũ nếu không có ảnh mới
            val imageUrls = mutableListOf<String>()
            
            // Giữ lại ảnh cũ nếu không có ảnh mới
            if (imageUris.isEmpty() && existingImageUrls.isNotEmpty()) {
                imageUrls.addAll(existingImageUrls)
            } else {
                // Upload ảnh mới
                imageUris.forEachIndexed { index, uri ->
                    try {
                        android.util.Log.d("UserRecipeRepo", "Uploading image $index of ${imageUris.size}")
                        val imageUrl = uploadImage(uri)
                        if (imageUrl != null) {
                            imageUrls.add(imageUrl)
                            android.util.Log.d("UserRecipeRepo", "Successfully uploaded image $index: $imageUrl")
                        } else {
                            android.util.Log.w("UserRecipeRepo", "Failed to upload image $index")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("UserRecipeRepo", "Error uploading image $index: ${e.message}", e)
                        // Continue with other images even if one fails
                    }
                }
            }
            
            // Convert ingredients to map
            val ingredientsData = ingredients.map { ingredient ->
                mapOf(
                    "name" to ingredient.name,
                    "quantity" to ingredient.quantity,
                    "unit" to ingredient.unit.abbreviation,
                    "foodItemId" to (ingredient.foodItemId ?: ""),
                    "categoryId" to (ingredient.categoryId ?: "")
                )
            }
            
            // Convert cooking steps to map
            val stepsData = cookingSteps.mapIndexed { index, step ->
                val stepImageUrl = if (step.imageUri != null) {
                    try {
                        step.imageUri?.let { uploadImage(it) } ?: ""
                    } catch (e: Exception) {
                        android.util.Log.e("UserRecipeRepo", "Error uploading step $index image: ${e.message}", e)
                        ""
                    }
                } else {
                    // Nếu không có ảnh mới, giữ lại ảnh cũ từ existing data
                    val existingSteps = doc.data?.get("cookingSteps") as? List<Map<String, Any>>
                    existingSteps?.getOrNull(index)?.get("imageUrl") as? String ?: ""
                }
                mapOf(
                    "stepNumber" to (index + 1),
                    "description" to step.description,
                    "imageUrl" to stepImageUrl
                )
            }
            
            // Update recipe document
            val updateData = hashMapOf<String, Any>(
                "recipeName" to recipeName,
                "estimatedTime" to estimatedTime,
                "servings" to servings,
                "imageUrls" to imageUrls,
                "ingredients" to ingredientsData,
                "cookingSteps" to stepsData,
                "description" to description,
                "notes" to notes,
                "tips" to tips,
                "nutritionData" to mapOf(
                    "calories" to nutritionData.calories,
                    "fat" to nutritionData.fat,
                    "carbs" to nutritionData.carbs,
                    "protein" to nutritionData.protein,
                    "cholesterol" to nutritionData.cholesterol,
                    "sodium" to nutritionData.sodium,
                    "vitamin" to nutritionData.vitamin,
                    "vitaminDetails" to mapOf(
                        "vitaminA" to nutritionData.vitaminDetails.vitaminA,
                        "vitaminB1" to nutritionData.vitaminDetails.vitaminB1,
                        "vitaminB2" to nutritionData.vitaminDetails.vitaminB2,
                        "vitaminB3" to nutritionData.vitaminDetails.vitaminB3,
                        "vitaminB6" to nutritionData.vitaminDetails.vitaminB6,
                        "vitaminB9" to nutritionData.vitaminDetails.vitaminB9,
                        "vitaminB12" to nutritionData.vitaminDetails.vitaminB12,
                        "vitaminC" to nutritionData.vitaminDetails.vitaminC,
                        "vitaminD" to nutritionData.vitaminDetails.vitaminD,
                        "vitaminE" to nutritionData.vitaminDetails.vitaminE,
                        "vitaminK" to nutritionData.vitaminDetails.vitaminK
                    )
                ),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            
            // Update to Firestore
            firestore.collection("userRecipes")
                .document(recipeId)
                .update(updateData)
                .await()
            
            android.util.Log.d("UserRecipeRepo", "Recipe updated successfully: $recipeId")
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("UserRecipeRepo", "Error updating recipe to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Lấy thông tin recipe theo ID
     */
    suspend fun getRecipeById(recipeId: String): Map<String, Any>? {
        return try {
            val doc = firestore.collection("userRecipes")
                .document(recipeId)
                .get()
                .await()
            
            doc.data
        } catch (e: Exception) {
            android.util.Log.e("UserRecipeRepo", "Error getting recipe: ${e.message}", e)
            null
        }
    }
}

