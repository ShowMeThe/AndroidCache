package com.show.cache.work

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.*
import com.show.cache.AppInit
import com.show.cache.CacheConfig
import com.show.cache.db.DataHelper
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class WorkJob {

    companion object {
        private val job by lazy { WorkJob() }
        fun getManager() = job
    }

    fun runJob() {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<BackJobWork>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(false)
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .apply {
                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setRequiresDeviceIdle(true)
                    }*/
                }
                .build())
            .build()
        WorkManager.getInstance(AppInit.getContext())
            .enqueueUniquePeriodicWork(
                "WorkJob",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
    }

}


class BackJobWork(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val list = DataHelper.getDao().getAllSourceInfo()
        for (source in list) {
            val file = File(source.path)
            if (file.exists()
                    .not() || source.usingCount < -abs(CacheConfig.getConfig().abandonTime)
            ) {
                //delete not exist file
                DataHelper.getDao().deleteSource(source)
                if(file.exists()){
                    file.delete()
                }
            } else {
                source.usingCount = source.usingCount - 1
                DataHelper.getDao().insertSource(source)
            }
        }
        return Result.success()
    }

}