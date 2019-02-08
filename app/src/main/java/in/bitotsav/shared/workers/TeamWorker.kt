package `in`.bitotsav.shared.workers

import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.workers.TeamWorkType.*
import `in`.bitotsav.teams.championship.data.ChampionshipTeamRepository
import `in`.bitotsav.teams.nonchampionship.data.NonChampionshipTeamRepository
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.get

private const val TAG = "TeamWorker"

enum class TeamWorkType {
    FETCH_TEAM,
    FETCH_BC_TEAM,
    FETCH_ALL_TEAMS,
    CLEAN_OLD_TEAMS
}

class TeamWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    override fun doWork(): Result {
        try {
            val type = inputData.getString("type")?.let { valueOf(it) }
                ?: throw NonRetryableException("Invalid work type")
            when (type) {
                FETCH_ALL_TEAMS -> runBlocking {
                    get<ChampionshipTeamRepository>().fetchAllChampionshipTeamsAsync().await()
                }
                FETCH_TEAM -> {
                    val eventId = inputData.getInt("eventId", -1)
                    if (eventId == -1)
                        throw NonRetryableException("Event id is empty")
                    val teamLeaderId = inputData.getString("teamLeaderId")
                        ?: throw NonRetryableException("Leader id is empty")
                    val isUserTeam = inputData.getBoolean("isUserTeam", false)
                    runBlocking {
                        get<NonChampionshipTeamRepository>()
                            .fetchNonChampionshipTeamAsync(eventId, teamLeaderId, isUserTeam).await()
                    }
                }
                CLEAN_OLD_TEAMS -> runBlocking {
                    get<NonChampionshipTeamRepository>().cleanupUserTeams()
                    Log.d(TAG, "Teams cleanup complete")
                }
                FETCH_BC_TEAM -> {
                    val teamName = inputData.getString("teamName")
//                    val user = User(
//                        CurrentUser.bitotsavId!!,
//                        CurrentUser.name!!,
//                        CurrentUser.email!!,
//                        teamName
//                    )
                    runBlocking {
                        //                        get<UserRepository>().insert(user)
//                        Log.d(TAG, "User inserted into DB")
                        teamName?.let {
                            get<ChampionshipTeamRepository>().fetchChampionshipTeamAsync(teamName).await()
                        } ?: Log.d(TAG, "Championship team not found for this user.")
                    }
                }
            }
            return Result.success()
        } catch (e: NonRetryableException) {
            Log.d(TAG, e.message ?: "Non-retryable exception")
            return Result.failure()
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Unknown Error")
            return Result.retry()
        }
    }
}