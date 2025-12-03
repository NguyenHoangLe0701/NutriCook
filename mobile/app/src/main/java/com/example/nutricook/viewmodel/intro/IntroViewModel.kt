package com.example.nutricook.viewmodel.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.preload.DataPreloadManager
import com.example.nutricook.view.intro.PreloadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel cho IntroScreen
 * Quản lý việc preload data và hiển thị progress
 */
@HiltViewModel
class IntroViewModel @Inject constructor(
    private val dataPreloadManager: DataPreloadManager
) : ViewModel() {

    private val _preloadState = MutableStateFlow<PreloadState>(PreloadState.IDLE)
    val preloadState: StateFlow<PreloadState> = _preloadState.asStateFlow()

    private val _preloadProgress = MutableStateFlow(0)
    val preloadProgress: StateFlow<Int> = _preloadProgress.asStateFlow()

    private val _preloadMessage = MutableStateFlow("Đang khởi tạo...")
    val preloadMessage: StateFlow<String> = _preloadMessage.asStateFlow()

    /**
     * Bắt đầu preload data
     */
    fun startPreload() {
        if (_preloadState.value == PreloadState.LOADING) return

        viewModelScope.launch {
            _preloadState.value = PreloadState.LOADING
            
            dataPreloadManager.preloadAllData(
                onProgress = { progress, message ->
                    _preloadProgress.value = progress
                    _preloadMessage.value = message
                },
                onComplete = {
                    _preloadState.value = PreloadState.COMPLETED
                },
                onError = { error ->
                    _preloadState.value = PreloadState.ERROR
                    _preloadMessage.value = "Lỗi: ${error.message}"
                }
            )
        }
    }
}




