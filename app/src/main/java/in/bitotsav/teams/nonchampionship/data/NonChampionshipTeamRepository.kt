package `in`.bitotsav.teams.nonchampionship.data

import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.profile.User
import `in`.bitotsav.shared.data.Repository
import `in`.bitotsav.teams.api.NonChampionshipTeamService
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.koin.core.KoinComponent
import org.koin.core.get

private const val TAG = "NonBCTeamRepository"

class NonChampionshipTeamRepository(
    private val nonChampionshipTeamDao: NonChampionshipTeamDao
) : Repository<NonChampionshipTeam>, KoinComponent {
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
    fun fetchNonChampionshipTeamAsync(eventId: Int, teamLeaderId: String): Deferred<Any> {
//    TODO: Needs thorough testing
        return CoroutineScope(Dispatchers.IO).async {
            val body = mapOf(
                "eventId" to eventId,
                "teamLeaderId" to teamLeaderId
            )
            val request = NonChampionshipTeamService.api.getNonChampionshipTeamAsync(body)
            val response = request.await()
            if (response.code() == 200) {
                Log.d(TAG, "Team retrieved from DB. eventId: $eventId, teamLeaderId: $teamLeaderId")
                @Suppress("UNCHECKED_CAST")
                val members = response.body()?.get("teamMembers") as Map<String, String>
//                TODO("Retrieve all parameters")
                val event = get<EventRepository>().getById(eventId)
//                eventPosition1: {
//                    teamLeader: req.body.eventPosition1,
//                    teamLeaderName: map[req.body.eventPosition1].name,
//                    championshipTeam: (team1 != null) ? team1 : "-1",
//                    points: (team1 != null) ? event.eventPoints1 : 0
//                }
                var userTeam = User.userTeams?.get(eventId.toString())
                val isUserTeam: Boolean

                if (userTeam == null) {
                    isUserTeam = false
                } else {
                    userTeam = userTeam as Map<String, String>
                    val userTeamLeader = userTeam["id"]
                        ?: throw Exception("Team leader for event: $eventId not found")
                    isUserTeam = (teamLeaderId == userTeamLeader)
                }

                if (isUserTeam) {
                    var userTeams = User.userTeams?.toMutableMap()
                    val team = (userTeams?.get(eventId.toString()) as Map<String, String>).toMutableMap()
                    team["id"] = teamLeaderId
                    team["name"] = members.values.first()
                    userTeams[eventId.toString()] = team
                    User.userTeams = userTeams.toMap()
                }

                var rank = 0
                var teamName: String? = null
                try {
                    if (teamLeaderId == event?.position1?.get("teamLeader")) {
                        rank = 1
                        if (event.position1.getValue("championshipTeam") != "-1")
                            teamName = event.position1["championsipTeam"]
                    } else if (teamLeaderId == event?.position2?.get("teamLeader")) {
                        rank = 2
                        if (event.position2.getValue("championshipTeam") != "-1")
                            teamName = event.position2["championsipTeam"]
                    } else if (teamLeaderId == event?.position3?.get("teamLeader")) {
                        rank = 3
                        if (event.position3.getValue("championshipTeam") != "-1")
                            teamName = event.position3["championsipTeam"]
                    }
                } catch (e: NoSuchElementException) {
                    Log.e(TAG, e.message)
                }

                if (teamName.isNullOrEmpty() || teamName == "-1") teamName = "${members.values.first()}'s team"

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