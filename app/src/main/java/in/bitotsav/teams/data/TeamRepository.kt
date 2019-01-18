package `in`.bitotsav.teams.data

import `in`.bitotsav.shared.data.Repository
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class TeamRepository(private val teamDao: TeamDao): Repository<Team> {
    override fun getAll(): LiveData<List<Team>> {
        return teamDao.getAll()
    }

    @WorkerThread
    fun getByName(name: String): Team {
        return teamDao.getByName(name)
    }

    @WorkerThread
    override suspend fun insert(vararg items: Team) {
        teamDao.insert(*items)
    }

    @WorkerThread
    suspend fun deleteUserTeams() {
        teamDao.deleteUserTeams()
    }
}