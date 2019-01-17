package `in`.bitotsav.events

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {
    @Query("SELECT * FROM event")
    fun getAll() : LiveData<List<Event>>

    @Query("SELECT * FROM event WHERE id = :id")
    fun getById(id: Int) : Event

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg events: Event)
}