package `in`.bitotsav.teams.nonchampionship.data

import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.shared.Singleton
import `in`.bitotsav.shared.data.Repository
import `in`.bitotsav.shared.network.NetworkException
import `in`.bitotsav.teams.api.NonChampionshipTeamService
import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val TAG = "NonBCTeamRepository"

class NonChampionshipTeamRepository(
    private val nonChampionshipTeamDao: NonChampionshipTeamDao
): Repository<NonChampionshipTeam> {
    override fun getAll(): LiveData<List<NonChampionshipTeam>> {
        return nonChampionshipTeamDao.getAll()
    }

    @WorkerThread
    suspend fun getById(eventId: Int, teamLeaderId: String): NonChampionshipTeam? {
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

//    POST - /getTeamDetails - {eventId, teamLeaderId}
//    502 - Server error
//    404 - Team not found
//    403 - eventId or teamLeaderId not found
//    200 - Success with teamMembers array

    fun fetchNonChampionshipTeam(eventId: Int, teamLeaderId: String, context: Context): Deferred<Any> {
        return CoroutineScope(Dispatchers.IO).async {
            val body = mapOf(
                "eventId" to eventId,
                "teamLeaderId" to teamLeaderId
            )
            val request = NonChampionshipTeamService.api.getNonChampionshipTeam(body)
            val response = request.await()
            if (response.code() == 200) {
                Log.d(TAG, "Team retrieved from DB. eventId: $eventId, teamLeaderId: $teamLeaderId")
                val members = response.body() ?: throw NetworkException("Response body is empty")
//                TODO("Retrieve all parameters")
                val eventDao = Singleton.database.getInstance(context).eventDao()
                val event = EventRepository(eventDao).getById(eventId)
                val team = NonChampionshipTeam(
                    eventId,
                    teamLeaderId,
                    "",
                    members,
                    0,
                    false
                )
                insert(team)
                Log.d(TAG, "Team inserted into DB")
            } else {
                when (response.code()) {
                    404 -> throw Exception("Team not found")
                    403 -> throw Exception("Evemt id or team leader id not found")
                    else -> throw Exception("Failed to retrieve {$eventId, $teamLeaderId}. Code: ${response.code()}")
                }
            }
        }
    }
}