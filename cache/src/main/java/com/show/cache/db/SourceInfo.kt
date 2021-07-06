package com.show.cache.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SourceInfo(
    @PrimaryKey val md5Name: String,
    val compress: Int = 100,
    val path: String,
    val finalMd5: String,
    var usingCount :Int = 1
)

