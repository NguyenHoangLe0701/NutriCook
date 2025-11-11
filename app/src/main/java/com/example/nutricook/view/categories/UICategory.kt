package com.example.nutricook.view.categories

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

data class UICategory(
    val id: String,
    val name: String,
    @DrawableRes val iconRes: Int,
    val color: Color
)
