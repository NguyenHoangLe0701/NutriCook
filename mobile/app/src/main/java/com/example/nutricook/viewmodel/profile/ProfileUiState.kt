package com.example.nutricook.viewmodel.profile

import com.example.nutricook.model.profile.Profile

data class ProfileUiState(
    val loading: Boolean = true,
    val updating: Boolean = false,
    val profile: Profile? = null,
    val message: String? = null,
    val chartData: List<Float> = emptyList()
)