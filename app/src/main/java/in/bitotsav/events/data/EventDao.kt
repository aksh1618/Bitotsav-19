package `in`.bitotsav.events.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {
    @Query("SELECT * FROM event")
    fun getAll(): LiveData<List<Event>>

    @Query("SELECT * FROM event")
    suspend fun getAllEvents(): List<Event>

    @Query("SELECT * FROM event WHERE isStarred == 1")
    suspend fun getAllStarred(): List<Event>

    @Query("SELECT DISTINCT category FROM event ")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT * FROM event WHERE day = :day")
    fun getByDay(day: Int): LiveData<List<Event>>

    @Query("SELECT * FROM event WHERE isStarred = 1 AND day = :day")
    fun getStarredByDay(day: Int): LiveData<List<Event>>

    @Query("SELECT * FROM event WHERE category IN(:categories)")
    fun getByCategories(vararg categories: String): LiveData<List<Event>>

    @Query("SELECT * FROM event WHERE category IN(:categories) AND day = :day")
    fun getByCategoriesForDay(day: Int, vararg categories: String): LiveData<List<Event>>

    @Query("SELECT * FROM event WHERE isStarred = 1 AND category IN(:categories) AND day = :day")
    fun getStarredByCategoriesForDay(day: Int, vararg categories: String): LiveData<List<Event>>

    @Query("SELECT * FROM event WHERE id = :id")
    suspend fun getById(id: Int): Event?

    @Query("SELECT name FROM event WHERE id = :id")
    suspend fun getNameById(id: Int): String?

    @Query("SELECT isStarred FROM event WHERE id = :id")
    suspend fun isStarred(id: Int): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg events: Event)
}