package `in`.bitotsav.shared.workers

import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.workers.TeamWorkType.*
import `in`.bitotsav.teams.championship.data.ChampionshipTeamRepository
import `in`.bitotsav.teams.nonchampionship.data.NonChampionshipTeamRepository
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.get

private const val TAG = "TeamWorker"

enum class TeamWorkType {
    FETCH_TEAM,
    FETCH_ALL_TEAMS,
    CLEAN_OLD_TEAMS
}

class TeamWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    override fun doWork(): Result {
        try {
            val type = inputData.getString("type")?.let { valueOf(it) }
                ?: return Result.failure(workDataOf("Error" to "Invalid work type"))
            when (type) {
                FETCH_ALL_TEAMS -> runBlocking {
                    get<ChampionshipTeamRepository>().fetchAllChampionshipTeamsAsync().await()
                }
                FETCH_TEAM -> {
                    val eventId = inputData.getInt("eventId", -1)
                    if (eventId == -1)
                        return Result.failure(workDataOf("Error" to "Event id is empty"))
                    val teamLeaderId = inputData.getString("teamLeaderId")
                        ?: return Result.failure(workDataOf("Error" to "Leader id is empty"))
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
            }
            return Result.success()
        } catch (e: NonRetryableException) {
            Log.d(TAG, e.message)
            return Result.failure()
        } catch (e: Exception) {
            Log.d(TAG, e.message)
            return Result.retry()
        }
    }
}