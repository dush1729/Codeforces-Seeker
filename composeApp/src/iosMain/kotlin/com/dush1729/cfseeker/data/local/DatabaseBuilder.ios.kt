package com.dush1729.cfseeker.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSHomeDirectory

fun createIosDatabase(): AppDatabase {
    val dbFilePath = NSHomeDirectory() + "/Documents/app_database"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
        .addMigrations(*AppDatabaseMigrations.ALL_MIGRATIONS)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}
