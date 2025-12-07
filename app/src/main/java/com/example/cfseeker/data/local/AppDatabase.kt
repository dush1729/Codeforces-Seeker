package com.example.cfseeker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cfseeker.data.local.dao.UserDao
import com.example.cfseeker.data.local.entity.RatingChangeEntity
import com.example.cfseeker.data.local.entity.UserEntity

@Database(
    version = 1,
    entities = [
        UserEntity::class,
        RatingChangeEntity::class,
    ],
    exportSchema = true,
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}