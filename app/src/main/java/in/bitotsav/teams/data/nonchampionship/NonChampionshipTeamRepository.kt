package `in`.bitotsav.teams.data.nonchampionship

import `in`.bitotsav.shared.data.Repository
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class NonChampionshipTeamRepository(
    private val nonChampionshipTeamDao: NonChampionshipTeamDao
): Repository<NonChampionshipTeam> {
    override fun getAll(): LiveData<List<NonChampionshipTeam>> {
        return nonChampionshipTeamDao.getAll()
    }

    @WorkerThread
    suspend fun getById(eventId: Int, teamLeaderId: String): NonChampionshipTeam {
        return nonChampionshipTeamDao.getById(eventId, teamLeaderId)
    }

    @WorkerThread
    override suspend fun insert(vararg items: NonChampionshipTeam) {
        nonChampionshipTeamDao.insert(*items)
    }

    @WorkerThread
    suspend fun deleteUserTeams() {
        nonChampionshipTeamDao.deleteUserTeams()
    }
}