package `in`.bitotsav.teams.championship.data

import `in`.bitotsav.shared.data.Repository
import `in`.bitotsav.shared.exceptions.NetworkException
import `in`.bitotsav.teams.api.ChampionshipTeamService
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val TAG = "BCTeamRepository"

class ChampionshipTeamRepository(private val championshipTeamDao: ChampionshipTeamDao) : Repository<ChampionshipTeam> {
    override fun getAll(): LiveData<List<ChampionshipTeam>> = championshipTeamDao.getAll()

    @WorkerThread
    suspend fun getByName(name: String): ChampionshipTeam? = championshipTeamDao.getByName(name)

    @WorkerThread
    suspend fun getScoreByName(name: String): Int? = championshipTeamDao.getScoreByName(name)

    @WorkerThread
    override suspend fun insert(vararg items: ChampionshipTeam) = championshipTeamDao.insert(*items)

    //    GET - /getAllBCTeams
//    502 - Server error
//    200 - Array of teams
    fun fetchAllChampionshipTeamsAsync(): Deferred<Any> {
        return CoroutineScope(Dispatchers.IO).async {
            val request = ChampionshipTeamService.api.getAllChampionshipTeamsAsync()
            val response = request.await()
            if (response.code() == 200) {
                val championshipTeams = response.body() ?: throw NetworkException("Response body is empty")
                val teamsByRank = getChampionshipTeamsByRank(championshipTeams)
                insert(*teamsByRank.toTypedArray())
                Log.d(TAG, "Inserted all championship teams into DB")
            } else {
                throw NetworkException("Fetch all championship teams failed. Code: ${response.code()}")
            }
        }
    }

    private fun getChampionshipTeamsByRank(teams: List<ChampionshipTeam>): List<ChampionshipTeam> {
        val sortedTeams = teams.sortedByDescending { it.totalScore }
        var rank = 1
        var jump = 0
        var previousScore = sortedTeams.first().totalScore
        sortedTeams.forEach {
            if (it.totalScore == previousScore) {
                it.rank = rank
                ++jump
            } else {
                it.rank = rank + jump
                rank = it.rank
                jump = 1
            }
            previousScore = it.totalScore
        }
        return sortedTeams
    }
}