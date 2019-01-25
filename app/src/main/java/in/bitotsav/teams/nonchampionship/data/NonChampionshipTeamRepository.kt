package `in`.bitotsav.teams.nonchampionship.data

import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.shared.Singleton
import `in`.bitotsav.shared.data.Repository
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
                @Suppress("UNCHECKED_CAST")
                val members = response.body()?.get("teamMembers") as Map<String, String>
//                TODO("Retrieve all parameters")
                val eventDao = Singleton.database.getInstance(context).eventDao()
                val event = EventRepository(eventDao).getById(eventId)
//                eventPosition1: {
//                    teamLeader: req.body.eventPosition1,
//                    teamLeaderName: map[req.body.eventPosition1].name,
//                    championshipTeam: (team1 != null) ? team1 : "-1",
//                    points: (team1 != null) ? event.eventPoints1 : 0
//                }
//                TODO("Get this data")
                val userTeamLeader = ""
                val userTeamName = ""
                val isUserTeam = (userTeamLeader == teamLeaderId)
                var rank = 0
                var teamName = "${members.values.first()}'s team"
                try {
                    if (event?.position1?.get("teamLeader") == userTeamLeader) {
                        rank = 1
                        if (event.position1.getValue("points").toInt() != 0) {
                            teamName = userTeamName
                        }
                    } else if (event?.position2?.get("teamLeader") == userTeamLeader) {
                        rank = 2
                        if (event.position2.getValue("points").toInt() != 0) {
                            teamName = userTeamName
                        }
                    } else if (event?.position3?.get("teamLeader") == userTeamLeader) {
                        rank = 2
                        if (event.position3.getValue("points").toInt() != 0) {
                            teamName = userTeamName
                        }
                    }
                } catch (e: NoSuchElementException) {
                    Log.d(TAG, e.message)
                }
                val team = NonChampionshipTeam(
                    eventId,
                    teamLeaderId,
                    teamName,
                    members,
                    rank,
                    isUserTeam
                )
                insert(team)
                Log.d(TAG, "Team inserted into DB")
            } else {
                when (response.code()) {
                    404 -> throw Exception("Team not found")
                    403 -> throw Exception("Event id or team leader id not found")
                    else -> throw Exception("Failed to retrieve {$eventId, $teamLeaderId}. Code: ${response.code()}")
                }
            }
        }
    }
}