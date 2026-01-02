package com.dush1729.cfseeker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dush1729.cfseeker.data.local.dao.ContestDao
import com.dush1729.cfseeker.data.local.dao.UserDao
import com.dush1729.cfseeker.data.local.entity.ContestEntity
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.local.entity.UserEntity

@Database(
    version = 3,
    entities = [
        UserEntity::class,
        RatingChangeEntity::class,
        ContestEntity::class,
    ],
    exportSchema = true,
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contestDao(): ContestDao
}