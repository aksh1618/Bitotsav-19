package `in`.bitotsav.teams.nonchampionship.data

import `in`.bitotsav.database.AppDatabase
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.shared.data.Repository
import `in`.bitotsav.shared.exceptions.NetworkException
import `in`.bitotsav.shared.exceptions.NonRetryableException
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

    override fun getAll(): LiveData<List<NonChampionshipTeam>> = nonChampionshipTeamDao.getAll()

    suspend fun getAllUserTeams(): List<NonChampionshipTeam>? = nonChampionshipTeamDao.getAllUserTeams()

    @WorkerThread
    suspend fun getById(eventId: Int, teamLeaderId: String): NonChampionshipTeam? =
        nonChampionshipTeamDao.getById(eventId, teamLeaderId)

    @WorkerThread
    override suspend fun insert(vararg items: NonChampionshipTeam) = nonChampionshipTeamDao.insert(*items)

    @WorkerThread
    suspend fun cleanupUserTeams() {
        get<AppDatabase>().beginTransaction()
        try {
            nonChampionshipTeamDao.deleteUserOldTeams()
            nonChampionshipTeamDao.changeTempStatus()

            get<AppDatabase>().setTransactionSuccessful()
        } finally {
            get<AppDatabase>().endTransaction()
        }
    }

    //    POST - /getTeamDetails - {eventId, teamLeaderId}
//    502 - Server error
//    404 - Team not found
//    403 - eventId or teamLeaderId not found
//    200 - Success with teamMembers array
    fun fetchNonChampionshipTeamAsync(eventId: Int, teamLeaderId: String, isUserTeam: Boolean): Deferred<Any> {
        // TODO: Needs thorough testing
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
                val event = get<EventRepository>().getById(eventId)
//                eventPosition1: {
//                    teamLeader: req.body.eventPosition1,
//                    teamLeaderName: map[req.body.eventPosition1].name,
//                    championshipTeam: (team1 != null) ? team1 : "-1",
//                    points: (team1 != null) ? event.eventPoints1 : 0
//                }

                if (isUserTeam) {
                    val userTeams = CurrentUser.userTeams?.toMutableMap()
                    val team = userTeams?.get(eventId.toString())?.toMutableMap()
                    team?.let {
                        team["leaderId"] = teamLeaderId
                        team["leaderName"] = members.values.first()
                        userTeams[eventId.toString()] = team.toMap()
                        CurrentUser.userTeams = userTeams.toMap()
                    }
                }

                var rank = 0
                var teamName: String? = null
                try {
                    if (teamLeaderId == event?.position1?.get("teamLeader")) {
                        rank = 1
                        if ("-1" != event.position1["championshipTeam"])
                            teamName = event.position1["championshipTeam"]
                    } else if (teamLeaderId == event?.position2?.get("teamLeader")) {
                        rank = 2
                        if ("-1" != event.position2["championshipTeam"])
                            teamName = event.position2["championshipTeam"]
                    } else if (teamLeaderId == event?.position3?.get("teamLeader")) {
                        rank = 3
                        if ("-1" != event.position3["championshipTeam"])
                            teamName = event.position3["championshipTeam"]
                    }
                } catch (e: NoSuchElementException) {
                    Log.e(TAG, e.message ?: "No such element exception")
                }

                if (teamName.isNullOrEmpty() || teamName == "-1") teamName = "${members.values.first()}'s team"

                val team = NonChampionshipTeam(
                    eventId,
                    teamLeaderId,
                    teamName,
                    members,
                    rank,
                    isUserTeam,
                    isTemp = isUserTeam
                )
                insert(team)
                Log.d(TAG, "Team inserted into DB")
            } else {
                when (response.code()) {
                    404 -> throw NonRetryableException("Team not found")
                    403 -> throw NonRetryableException("Event id or team leader id not found")
                    else -> throw NetworkException(
                        "Failed to retrieve {$eventId, $teamLeaderId}. Response code: ${response.code()}"
                    )
                }
            }
        }
    }
}