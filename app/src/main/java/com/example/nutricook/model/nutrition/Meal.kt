package com.example.nutricook.model.nutrition

import com.google.firebase.Timestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Meal(
    val kcal: Int = 0,
    val proteinG: Double = 0.0,
    val fatG: Double = 0.0,
    val carbG: Double = 0.0,
    @Contextual val ateAt: Timestamp? = null,
    val note: String? = null,
    val id: String = ""
)

