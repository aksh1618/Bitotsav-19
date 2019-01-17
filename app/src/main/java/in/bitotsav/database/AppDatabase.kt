package `in`.bitotsav.database

import `in`.bitotsav.events.Event
import `in`.bitotsav.events.EventDao
import `in`.bitotsav.feed.FeedDao
import `in`.bitotsav.teams.TeamDao
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Event::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun eventDao(): EventDao

    abstract fun teamDao(): TeamDao

    abstract fun feedDao() : FeedDao
}