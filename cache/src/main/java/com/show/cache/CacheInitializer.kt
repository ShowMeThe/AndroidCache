package com.show.cache

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import com.show.cache.db.DataHelper
import com.show.cache.work.WorkJob
import java.util.*

class CacheInitializer : Initializer<AppInit> {

    override fun create(context: Context): AppInit {
        val appInit = AppInit.attach(context)
        WorkManager.initialize(context, Configuration.Builder().build())
        WorkJob.getManager().runJob()
        return appInit
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = Collections.emptyList()
}