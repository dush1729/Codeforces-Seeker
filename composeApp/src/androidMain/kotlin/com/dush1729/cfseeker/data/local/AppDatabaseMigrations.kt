package com.dush1729.cfseeker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_handle ON user(handle)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_lastSync ON user(lastSync)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_rating ON user(rating)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_rating_change_handle ON rating_change(handle)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_rating_change_ratingUpdateTimeSeconds ON rating_change(ratingUpdateTimeSeconds)")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `contest` (
                    `id` INTEGER PRIMARY KEY NOT NULL,
                    `name` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `phase` TEXT NOT NULL,
                    `frozen` INTEGER NOT NULL,
                    `durationSeconds` INTEGER NOT NULL,
                    `startTimeSeconds` INTEGER NOT NULL,
                    `relativeTimeSeconds` INTEGER NOT NULL
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_contest_id ON contest(id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_contest_phase ON contest(phase)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_contest_startTimeSeconds ON contest(startTimeSeconds)")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP INDEX IF EXISTS index_contest_id")
            db.execSQL("DROP INDEX IF EXISTS index_contest_phase")
            db.execSQL("DROP INDEX IF EXISTS index_contest_startTimeSeconds")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_contest_phase_startTime ON contest(phase, startTimeSeconds)")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `contest_problem` (
                    `contestId` INTEGER NOT NULL,
                    `problemsetName` TEXT,
                    `index` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `points` REAL,
                    `rating` INTEGER,
                    `tags` TEXT NOT NULL,
                    PRIMARY KEY(`contestId`, `index`)
                )
            """)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `contest_standing_row` (
                    `contestId` INTEGER NOT NULL,
                    `rank` INTEGER NOT NULL,
                    `points` REAL NOT NULL,
                    `penalty` INTEGER NOT NULL,
                    `successfulHackCount` INTEGER NOT NULL,
                    `unsuccessfulHackCount` INTEGER NOT NULL,
                    `lastSubmissionTimeSeconds` INTEGER,
                    `participantType` TEXT NOT NULL,
                    `teamId` INTEGER,
                    `teamName` TEXT,
                    `ghost` INTEGER NOT NULL,
                    `room` INTEGER,
                    `memberHandles` TEXT NOT NULL,
                    `problemResults` TEXT NOT NULL,
                    PRIMARY KEY(`contestId`, `rank`)
                )
            """)
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `contest_standing_row`")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `contest_standing_row` (
                    `contestId` INTEGER NOT NULL,
                    `rank` INTEGER NOT NULL,
                    `points` REAL NOT NULL,
                    `penalty` INTEGER NOT NULL,
                    `successfulHackCount` INTEGER NOT NULL,
                    `unsuccessfulHackCount` INTEGER NOT NULL,
                    `lastSubmissionTimeSeconds` INTEGER,
                    `participantType` TEXT NOT NULL,
                    `teamId` INTEGER,
                    `teamName` TEXT,
                    `ghost` INTEGER NOT NULL,
                    `room` INTEGER,
                    `memberHandles` TEXT NOT NULL,
                    `problemResults` TEXT NOT NULL,
                    PRIMARY KEY(`contestId`, `memberHandles`)
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_contest_standing_contestId_rank ON contest_standing_row(contestId, rank)")
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `rating_change` ADD COLUMN `source` TEXT NOT NULL DEFAULT 'USER'")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_rating_change_contestId_source ON rating_change(contestId, source)")
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP INDEX IF EXISTS index_rating_change_handle")
            db.execSQL("DROP INDEX IF EXISTS index_rating_change_ratingUpdateTimeSeconds")
            db.execSQL("DROP INDEX IF EXISTS index_rating_change_contestId_source")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_rating_change_handle_source_time ON rating_change(handle, source, ratingUpdateTimeSeconds)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_rating_change_contestId_rank ON rating_change(contestId, contestRank)")
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP INDEX IF EXISTS index_user_handle")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_handle ON user(handle COLLATE NOCASE)")
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP INDEX IF EXISTS index_rating_change_handle_source_time")
            db.execSQL("DROP INDEX IF EXISTS index_rating_change_handle_source_ratingUpdateTimeSeconds")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_rating_change_source_handle_ratingUpdateTimeSeconds ON rating_change(source, handle, ratingUpdateTimeSeconds)")
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE VIEW `user_with_latest_rating_change` AS SELECT
            u.handle,
            u.avatar,
            u.city,
            u.contribution,
            u.country,
            u.email,
            u.firstName,
            u.friendOfCount,
            u.lastName,
            u.lastOnlineTimeSeconds,
            u.maxRank,
            u.maxRating,
            u.organization,
            u.rank,
            u.rating,
            u.registrationTimeSeconds,
            u.titlePhoto,
            u.lastSync,
            rc.contestId AS latestContestId,
            rc.contestName AS latestContestName,
            rc.contestRank AS latestContestRank,
            rc.oldRating AS latestOldRating,
            rc.newRating AS latestNewRating,
            rc.ratingUpdateTimeSeconds AS latestRatingUpdateTimeSeconds,
            CASE WHEN u.rating IS NOT rc.newRating THEN 1 ELSE 0 END AS isRatingOutdated
        FROM user u
        LEFT JOIN rating_change rc ON u.handle = rc.handle
            AND rc.source = 'USER'
            AND rc.ratingUpdateTimeSeconds = (
                SELECT MAX(rc2.ratingUpdateTimeSeconds)
                FROM rating_change rc2
                WHERE rc2.handle = u.handle AND rc2.source = 'USER'
            )""")
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP INDEX IF EXISTS index_rating_change_handle_source_ratingUpdateTimeSeconds")
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
        MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12
    )
}
