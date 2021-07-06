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
import java.io.BufferedReader
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

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
                CacheFlow.streamToCache(resources.assets.open("jtbz_weimei_4.jpg"))
                    .map {
                        BitmapFactory.decodeFile(it?.path)
                    }
                    .collect {
                        withContext(Dispatchers.Main.immediate) {
                            binding.iv.setImageBitmap(it)
                        }
                    }
            }
        }


    }
}