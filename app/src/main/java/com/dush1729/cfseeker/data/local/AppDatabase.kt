package com.dush1729.cfseeker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dush1729.cfseeker.data.local.AppDatabase.Companion.VERSION
import com.dush1729.cfseeker.data.local.dao.ContestDao
import com.dush1729.cfseeker.data.local.dao.ContestStandingsDao
import com.dush1729.cfseeker.data.local.dao.UserDao
import com.dush1729.cfseeker.data.local.entity.ContestEntity
import com.dush1729.cfseeker.data.local.entity.ContestProblemEntity
import com.dush1729.cfseeker.data.local.entity.ContestStandingRowEntity
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.data.local.view.UserWithLatestRatingChangeView

@Database(
    version = VERSION,
    entities = [
        UserEntity::class,
        RatingChangeEntity::class,
        ContestEntity::class,
        ContestProblemEntity::class,
        ContestStandingRowEntity::class,
    ],
    views = [
        UserWithLatestRatingChangeView::class,
    ],
    exportSchema = true,
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contestDao(): ContestDao
    abstract fun contestStandingsDao(): ContestStandingsDao

    companion object {
        const val VERSION = 12
    }
}