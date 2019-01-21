package `in`.bitotsav.teams.data.championship

import `in`.bitotsav.shared.data.Repository
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class ChampionshipTeamRepository(private val championshipTeamDao: ChampionshipTeamDao): Repository<ChampionshipTeam> {
    override fun getAll(): LiveData<List<ChampionshipTeam>> {
        return championshipTeamDao.getAll()
    }

    @WorkerThread
    fun getByName(name: String): ChampionshipTeam {
        return championshipTeamDao.getByName(name)
    }

    @WorkerThread
    override suspend fun insert(vararg items: ChampionshipTeam) {
        championshipTeamDao.insert(*items)
    }
}