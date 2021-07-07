package com.show.example

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.asLiveData
import com.show.cache.CacheConfig
import com.show.cache.CacheFlow
import com.show.example.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
                var startTime = System.currentTimeMillis()
                flow {
                    delay(500)
                    emit("data")
                }.combine(flow {
                    delay(400)
                    emit(111)
                },){ t1,t2 ->
                   return@combine t1 + "$t2"
                }.collect {
                    var endTime = System.currentTimeMillis()
                    Log.e("2222222222","${endTime - startTime}")
                }
            }
        }


    }
}