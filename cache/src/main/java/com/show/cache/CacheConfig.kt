package com.show.cache

class CacheConfig private constructor(){

    companion object{
        private val configs by lazy { CacheConfig() }

        fun newConfig(block : CacheConfig.()->Unit){
            block.invoke(configs)
        }

        fun getConfig() = configs

    }

    var rootName = "Android-Cache"

    var image = "Image"

    var file = "Files"

    var abandonTime = 2

}