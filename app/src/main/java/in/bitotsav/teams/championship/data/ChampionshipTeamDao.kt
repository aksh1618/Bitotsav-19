package `in`.bitotsav.teams.championship.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChampionshipTeamDao {
    @Query("SELECT * FROM championshipteam")
    fun getAll(): LiveData<List<ChampionshipTeam>>

    @Query("SELECT * FROM championshipteam WHERE name = :name")
    suspend fun getByName(name: String): ChampionshipTeam?

    @Query("SELECT totalScore FROM championshipteam WHERE name = :name")
    suspend fun getScoreByName(name: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg championshipTeams: ChampionshipTeam)
}