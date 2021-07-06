package com.show.cache

import android.graphics.Bitmap
import java.security.MessageDigest

object Util {

    fun getImageSuffix(format:Bitmap.CompressFormat):String{
        return when(format){
            Bitmap.CompressFormat.JPEG -> "jpeg"
            Bitmap.CompressFormat.WEBP -> "webp"
            Bitmap.CompressFormat.PNG, -> "png"
            else -> "jpeg"
        }
    }

    fun value2MD5(inStr: ByteArray): String {
        var md5: MessageDigest? = null
        try {
            md5 = MessageDigest.getInstance("MD5")
        } catch (e: Exception) {
            println(e.toString())
            e.printStackTrace()
            return ""
        }

        val charArray = inStr
        val byteArray = ByteArray(charArray.size)

        for (i in charArray.indices)
            byteArray[i] = charArray[i]
        val md5Bytes = md5!!.digest(byteArray)
        val hexValue = StringBuffer()
        for (i in md5Bytes.indices) {
            val `val` = md5Bytes[i].toInt() and 0xff
            if (`val` < 16)
                hexValue.append("0")
            hexValue.append(Integer.toHexString(`val`))
        }
        return hexValue.toString()

    }

}