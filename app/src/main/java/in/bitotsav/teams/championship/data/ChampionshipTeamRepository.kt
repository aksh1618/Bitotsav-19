package `in`.bitotsav.teams.championship.data

import `in`.bitotsav.R
import `in`.bitotsav.shared.data.Repository
import `in`.bitotsav.shared.data.getJsonStringFromFile
import `in`.bitotsav.shared.exceptions.NetworkException
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.teams.api.ChampionshipTeamService
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.google.gson.Gson
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

    fun getTeamsFromLocalJson() {
        val teamsJsonString = getJsonStringFromFile(R.raw.teams)
        val listOfTeams = Gson().fromJson(teamsJsonString, Array<ChampionshipTeam>::class.java).toList()
        CoroutineScope(Dispatchers.IO).async {
            insert(*getChampionshipTeamsByRank(listOfTeams).toTypedArray())
            Log.d(TAG, "Inserted teams into DB from local json file.")
        }
    }

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
                throw NetworkException("Fetch all championship teams failed. Response code: ${response.code()}")
            }
        }
    }

    fun fetchChampionshipTeamAsync(teamName: String): Deferred<Any> {
        return CoroutineScope(Dispatchers.IO).async {
            val body = mapOf("teamName" to teamName)
            val request = ChampionshipTeamService.api.getChampionshipTeamByNameAsync(body)
            val response = request.await()
            if (response.code() == 200) {
                val championshipTeam = response.body() ?: throw NetworkException("Response body is empty")
                insert(championshipTeam)
                Log.d(TAG, "Inserted team: $teamName into DB")
            } else {
                when (response.code()) {
                    403 -> throw NonRetryableException("Team name not sent in body")
                    404 -> throw NonRetryableException("Team name not found")
                    else -> throw NetworkException("Unable to fetch team: $teamName. Response code: ${response.code()}")
                }
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