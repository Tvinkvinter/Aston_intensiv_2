package com.atarusov.aston_intensiv_2

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.atarusov.aston_intensiv_2.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            viewModel.scale.collect { scale ->
                binding.wheel.scale = scale
            }
        }

        lifecycleScope.launch {
            viewModel.displayedView.collect { viewType ->
                resetViews()
                when (viewType) {
                    SpinningWheel.SectorType.TEXT -> binding.text.visibility = View.VISIBLE
                    SpinningWheel.SectorType.IMG -> {
                        loadImg()
                        binding.img.visibility = View.VISIBLE
                    }

                    null -> {}
                }
            }
        }

        binding.wheel.setOnClickListener {
            (it as SpinningWheel).spinWheel()
        }

        binding.wheel.setOnSectorStopListener { sectorType ->
            when (sectorType) {
                SpinningWheel.SectorType.TEXT -> viewModel.showText()
                SpinningWheel.SectorType.IMG -> viewModel.showImg()
            }
        }

        binding.slider.addOnChangeListener { _, value, _ ->
            viewModel.onSliderChanged(value)
        }

        binding.resetBtn.setOnClickListener {
            viewModel.resetViews()
        }
    }

    private fun loadImg() {
        Glide.with(this)
            .load("https://placebear.com/1280/720")
            .transform(CenterCrop(), RoundedCorners(16))
            .placeholder(R.drawable.ic_loading_24)
            .error(R.drawable.ic_image_error_24)
            .into(binding.img)
    }

    private fun resetViews() {
        binding.text.visibility = View.INVISIBLE
        binding.img.visibility = View.INVISIBLE
    }
}