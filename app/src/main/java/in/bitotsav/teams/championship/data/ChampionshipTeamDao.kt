package `in`.bitotsav.teams.championship.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChampionshipTeamDao {
    @Query("SELECT * FROM championshipteam")
    fun getAll() : LiveData<List<ChampionshipTeam>>

    @Query("SELECT * FROM championshipteam WHERE name = :name")
    fun getByName(name: String) : ChampionshipTeam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg championshipTeams: ChampionshipTeam)
}