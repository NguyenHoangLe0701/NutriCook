package com.example.nutricook.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val NutriCookShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),   // TextField / Button / Card
    large = RoundedCornerShape(16.dp),    // Card to
    extraLarge = RoundedCornerShape(24.dp)
)
