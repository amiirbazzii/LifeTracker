package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TimelineMeta::class,
        DailyTask::class,
        Category::class,
        Routine::class,
        Reward::class,
        SubGoal::class,
        DailyHabit::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timelineDao(): TimelineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_tasks ADD COLUMN routine_id TEXT DEFAULT NULL")
                db.execSQL("CREATE TABLE IF NOT EXISTS categories (category_id TEXT PRIMARY KEY NOT NULL, category_name TEXT NOT NULL, created_timestamp INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS routines (routine_id TEXT PRIMARY KEY NOT NULL, category_id TEXT NOT NULL, routine_title TEXT NOT NULL, target_count INTEGER NOT NULL, completed_count INTEGER NOT NULL DEFAULT 0, created_timestamp INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS rewards (reward_id TEXT PRIMARY KEY NOT NULL, reward_name TEXT NOT NULL, point_cost INTEGER NOT NULL, claimed_count INTEGER NOT NULL DEFAULT 0, created_timestamp INTEGER NOT NULL)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS sub_goals (sub_goal_id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL, duration_months INTEGER NOT NULL, start_month INTEGER NOT NULL DEFAULT 0, created_timestamp INTEGER NOT NULL)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_tasks ADD COLUMN habit_id TEXT DEFAULT NULL")
                db.execSQL("CREATE TABLE IF NOT EXISTS daily_habits (id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL, createdTimestamp INTEGER NOT NULL, isActive INTEGER NOT NULL DEFAULT 1)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_tasks ADD COLUMN start_time TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE daily_tasks ADD COLUMN end_time TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_1_5 = object : Migration(1, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_tasks ADD COLUMN routine_id TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE daily_tasks ADD COLUMN habit_id TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE daily_tasks ADD COLUMN start_time TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE daily_tasks ADD COLUMN end_time TEXT DEFAULT NULL")
                db.execSQL("CREATE TABLE IF NOT EXISTS categories (category_id TEXT PRIMARY KEY NOT NULL, category_name TEXT NOT NULL, created_timestamp INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS routines (routine_id TEXT PRIMARY KEY NOT NULL, category_id TEXT NOT NULL, routine_title TEXT NOT NULL, target_count INTEGER NOT NULL, completed_count INTEGER NOT NULL DEFAULT 0, created_timestamp INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS rewards (reward_id TEXT PRIMARY KEY NOT NULL, reward_name TEXT NOT NULL, point_cost INTEGER NOT NULL, claimed_count INTEGER NOT NULL DEFAULT 0, created_timestamp INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS sub_goals (sub_goal_id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL, duration_months INTEGER NOT NULL, start_month INTEGER NOT NULL DEFAULT 0, created_timestamp INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS daily_habits (id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL, createdTimestamp INTEGER NOT NULL, isActive INTEGER NOT NULL DEFAULT 1)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lifetracker_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_1_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
