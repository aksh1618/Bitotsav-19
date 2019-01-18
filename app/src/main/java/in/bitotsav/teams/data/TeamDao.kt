package `in`.bitotsav.teams.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TeamDao {
    @Query("SELECT * FROM team")
    fun getAll() : LiveData<List<Team>>

    @Query("SELECT * FROM team WHERE name = :name")
    fun getByName(name: String) : Team

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg teams: Team)

    @Query("DELETE FROM team WHERE eventId <> 0")
    fun deleteUserTeams()
}