package com.atarusov.aston_intensiv_2

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel: ViewModel() {
    private val _displayedView = MutableStateFlow<SpinningWheel.SectorType?>(null)
    val displayedView: StateFlow<SpinningWheel.SectorType?> = _displayedView

    private val _scale = MutableStateFlow(0.5f)
    val scale: StateFlow<Float> = _scale

    fun showText() {
        _displayedView.value = SpinningWheel.SectorType.TEXT
    }

    fun showImg() {
        _displayedView.value = SpinningWheel.SectorType.IMG
    }

    fun onSliderChanged(value: Float) {
        _scale.value = value / 100
    }

    fun resetViews() {
        _displayedView.value = null
    }

}