package com.show.cache

import android.content.Context

class AppInit {

    companion object{
        private val init by lazy { AppInit() }


        fun attach(context: Context):AppInit{
            init.context = context
            return init
        }

        fun getContext() = init.context
    }

    private lateinit var context: Context

    fun getContext() = this.context



}