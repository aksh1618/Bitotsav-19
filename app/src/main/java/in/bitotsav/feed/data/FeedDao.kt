package `in`.bitotsav.feed.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedDao {
    @Query("SELECT * FROM feed")
    fun getAll(): LiveData<List<Feed>>

    @Query("SELECT MAX(timestamp) FROM feed")
    fun getLatestTimestamp(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg feeds: Feed)
}