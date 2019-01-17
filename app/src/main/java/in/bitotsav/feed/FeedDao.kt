package `in`.bitotsav.feed

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedDao {
    @Query("SELECT * FROM feed")
    fun getAll() : LiveData<List<Feed>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg feeds: Feed)
}