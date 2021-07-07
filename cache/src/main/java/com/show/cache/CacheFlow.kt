package com.show.cache

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.IntRange
import androidx.core.graphics.BitmapCompat
import com.show.cache.db.DataHelper
import com.show.cache.db.SourceInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object CacheFlow {

    private val cacheDir by lazy { CacheConfig.getConfig() }

    private fun getCacheDir(): File? {
        val cacheFileDir = AppInit.getContext().externalCacheDir
        var rootFile: File? = null
        if (cacheFileDir != null) {
            rootFile = File(cacheFileDir.path + File.separator + cacheDir.rootName)
            if (rootFile.exists().not()) {
                rootFile.mkdirs()
            }
        }
        return rootFile
    }

    private fun checkDirIfNotCreate(middleName: String): File {
        val file = File(getCacheDir()?.path + File.separator + middleName)
        if (file.exists().not()) {
            file.mkdirs()
        }
        return file
    }

    fun streamAppendCache(inputStream: InputStream,file: File): Flow<File?> {
        return flow {
            val buffer = inputStream.buffered().readBytes()
            file.appendBytes(buffer)
            emit(file)
            kotlin.runCatching {
                inputStream.close()
            }
        }
    }


    fun streamToCache(inputStream: InputStream): Flow<File?> {
        return flow {
            val byteArray = inputStream.buffered().readBytes()
            val md5Name = Util.value2MD5(byteArray)
            emit(copyStreamToLocal(md5Name, byteArray))
            kotlin.runCatching {
                inputStream.close()
            }
        }
    }

    private fun copyStreamToLocal(
        md5Name: String,
        byteArray: ByteArray
    ): File? {
        return DataHelper.getDao().getSourceInfo(md5Name,100).let {
            if (it == null || File(it.path).exists().not()) {
                runCatching {
                    val cacheFile = File(
                        checkDirIfNotCreate(CacheConfig.getConfig().file).path + File.separator + UUID.randomUUID())
                    val fos = cacheFile.outputStream().buffered()
                    fos.write(byteArray)
                    fos.flush()
                    fos.close()
                    DataHelper.getDao().insertSource(
                        SourceInfo(
                            md5Name,
                            compress = 100,
                            path = cacheFile.path,
                            finalMd5 = Util.value2MD5(cacheFile.readBytes())
                        )
                    )
                    cacheFile
                }.onFailure { e ->
                    e.printStackTrace()
                }.getOrNull()
            } else {
                it.usingCount += 1
                DataHelper.getDao().insertSource(it)
                File(it.path)
            }
        }
    }


    fun fileToCache(file: File): Flow<File?> {
        return flow {
            val byteArray = file.readBytes()
            val md5Name = Util.value2MD5(byteArray)
            emit(copyFileToLocal(md5Name, byteArray,file.name))
        }
    }

    private fun copyFileToLocal(md5Name: String, byteArray : ByteArray,fileName:String): File? {
        return DataHelper.getDao().getSourceInfo(md5Name,100).let {
            if (it == null || File(it.path).exists().not()) {
                runCatching {
                    val cacheFile = File(
                        checkDirIfNotCreate(CacheConfig.getConfig().file).path + File.separator + UUID.randomUUID() + ".${
                            fileName.substringAfterLast(".")
                        }"
                    )
                    val fos = cacheFile.outputStream().buffered()
                    fos.write(byteArray)
                    fos.flush()
                    fos.close()
                    DataHelper.getDao().insertSource(
                        SourceInfo(
                            md5Name,
                            compress = 100,
                            path = cacheFile.path,
                            finalMd5 = Util.value2MD5(cacheFile.readBytes())
                        )
                    )
                    cacheFile
                }.onFailure { e ->
                    e.printStackTrace()
                }.getOrNull()
            } else {
                it.usingCount += 1
                DataHelper.getDao().insertSource(it)
                File(it.path)
            }
        }
    }


    fun bitmapToCache(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.WEBP,
        @IntRange(from = 0, to = 100) quality: Int = 70
    ): Flow<File?> {
        return flow {
            val md5Name = Util.value2MD5(ByteBuffer.allocate(bitmap.byteCount).let {
                bitmap.copyPixelsToBuffer(it)
                it.array()
            }) + "[${bitmap.width}X${bitmap.height}X${format.name}]"
            emit(copyBitmapToLocal(md5Name, bitmap, format, quality))
        }
    }


    fun bitmapToCache(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.WEBP,
        minWidth: Int = bitmap.width,
        minHeight: Int = bitmap.height,
        @IntRange(from = 0, to = 100) quality: Int = 70
    ): Flow<File?> {
        return flow {
            val minWidthScale = minWidth / bitmap.width.toFloat()
            val minHeightScale = minHeight / bitmap.height.toFloat()
            val scale = kotlin.math.min(minWidthScale, minHeightScale)
            val scaleBitmap = Bitmap.createScaledBitmap(
                bitmap,
                (scale * bitmap.width).toInt(),
                (scale * bitmap.height).toInt(),
                false
            )
            val md5Name = Util.value2MD5(ByteBuffer.allocate(scaleBitmap.byteCount).let {
                scaleBitmap.copyPixelsToBuffer(it)
                it.array()
            }) + "[${minWidth}X${minHeight}X${format.name}]"
            emit(copyBitmapToLocal(md5Name, scaleBitmap, format, quality, needRecycle = true))
        }
    }


    private fun copyBitmapToLocal(
        md5Name: String,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        quality: Int,
        needRecycle: Boolean = false
    ): File? {
        return DataHelper.getDao()
            .getSourceInfo(md5Name, quality)
            .let {
                if (it == null || (File(it.path).exists().not())) {
                    kotlin.runCatching {
                        val cacheFile = File(
                            checkDirIfNotCreate(CacheConfig.getConfig().image).path + File.separator + UUID.randomUUID() + ".${
                                Util.getImageSuffix(format)
                            }"
                        )
                        val fos = BufferedOutputStream(FileOutputStream(cacheFile))
                        bitmap.compress(format, quality, fos)
                        fos.flush()
                        fos.close()
                        DataHelper.getDao().insertSource(
                            SourceInfo(
                                md5Name,
                                quality,
                                cacheFile.path,
                                Util.value2MD5(cacheFile.readBytes())
                            )
                        )
                        if (needRecycle && bitmap.isRecycled.not()) {
                            bitmap.recycle()
                        }
                        cacheFile
                    }.onFailure { e ->
                        e.printStackTrace()
                    }.getOrNull()
                } else {
                    it.usingCount += 1
                    DataHelper.getDao().insertSource(it)
                    File(it.path)
                }
            }
    }


}