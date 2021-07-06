package com.show.cache.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.show.cache.AppInit
import com.show.cache.CacheConfig


@Database(entities = [SourceInfo::class],version = 1)
abstract class DatabaseCreator : RoomDatabase() {

   abstract fun sourceDao() : SourceDao

}


object DataHelper{

   private val  creator: DatabaseCreator by lazy { Room.databaseBuilder(AppInit.getContext().applicationContext,
      DatabaseCreator::class.java, CacheConfig.getConfig().rootName).build() }

   fun getDao() = creator.sourceDao()


}