package `in`.bitotsav.database

import `in`.bitotsav.events.data.Event
import `in`.bitotsav.events.data.EventDao
import `in`.bitotsav.feed.data.Feed
import `in`.bitotsav.feed.data.FeedDao
import `in`.bitotsav.shared.data.Converters
import `in`.bitotsav.teams.data.championship.ChampionshipTeam
import `in`.bitotsav.teams.data.championship.ChampionshipTeamDao
import `in`.bitotsav.teams.data.nonchampionship.NonChampionshipTeam
import `in`.bitotsav.teams.data.nonchampionship.NonChampionshipTeamDao
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Event::class, Feed::class, ChampionshipTeam::class, NonChampionshipTeam::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun eventDao(): EventDao

    abstract fun championshipTeamDao(): ChampionshipTeamDao

    abstract fun nonChampionshipTeamDao(): NonChampionshipTeamDao

    abstract fun feedDao(): FeedDao
}