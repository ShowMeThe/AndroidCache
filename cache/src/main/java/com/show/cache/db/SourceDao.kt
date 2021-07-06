package com.show.cache.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSource(sourceInfo: SourceInfo)


    @Query("select * from SourceInfo where md5Name=:md5Name and compress = :compress")
    fun getSourceInfo(md5Name: String, compress: Int): SourceInfo?

    @Delete
    fun deleteSource(sourceInfo: SourceInfo)

    @Query("select * from SourceInfo")
    fun getAllSourceInfo(): List<SourceInfo>
}