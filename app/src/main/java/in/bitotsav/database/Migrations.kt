package `in`.bitotsav.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE User ADD COLUMN day1 INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE User ADD COLUMN day2 INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE User ADD COLUMN day3 INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE User ADD COLUMN merchandise INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE User ADD COLUMN accommodation INTEGER NOT NULL DEFAULT 0")
    }

}