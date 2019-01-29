package `in`.bitotsav.teams.nonchampionship.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NonChampionshipTeamDao {
    @Query("SELECT * FROM nonchampionshipteam")
    fun getAll() : LiveData<List<NonChampionshipTeam>>

    @Query("SELECT * FROM nonchampionshipteam WHERE eventId = :eventId and teamLeaderId = :teamLeaderId")
    fun getById(eventId: Int, teamLeaderId: String) : NonChampionshipTeam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg nonChampionshipTeams: NonChampionshipTeam)

    @Query("DELETE FROM nonchampionshipteam WHERE isUserTeam == 1 AND isTemp == 0")
    fun deleteUserOldTeams()

    @Query("UPDATE nonchampionshipteam SET isTemp = 0 WHERE isTemp == 1")
    fun changeTempStatus()
}