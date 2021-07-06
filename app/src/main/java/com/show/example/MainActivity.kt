package com.show.example

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.asLiveData
import com.show.cache.CacheConfig
import com.show.cache.CacheFlow
import com.show.example.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CacheConfig.newConfig {
            abandonTime = 1
        }

        binding.btn.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_photo)
                CacheFlow.bitmapToCache(bitmap, minWidth = 1080, quality = 50)
                    .map { BitmapFactory.decodeFile(it?.path) }
                    .collect {
                        withContext(Dispatchers.Main.immediate) {
                            binding.iv.setImageBitmap(it)
                        }
                    }
            }
        }




    }
}