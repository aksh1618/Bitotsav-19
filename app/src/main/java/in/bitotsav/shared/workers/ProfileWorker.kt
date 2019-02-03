package `in`.bitotsav.shared.workers

import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.utils.AuthException
import `in`.bitotsav.profile.utils.fetchProfileDetailsAsync
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.utils.getWork
import `in`.bitotsav.shared.workers.ProfileWorkType.valueOf
import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.runBlocking

private const val TAG = "ProfileWorker"

enum class ProfileWorkType {
    FETCH_PROFILE,
}

class ProfileWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        try {
            val type = inputData.getString("type")?.let { valueOf(it) }
                ?: throw NonRetryableException("Invalid work type")
            val authToken = CurrentUser.authToken
                ?: throw NonRetryableException("Auth token is empty")
            runBlocking { fetchProfileDetailsAsync(authToken).await() }
            Log.d(TAG, "Fetching completed")
            fetchUserTeams()
            return Result.success()
        } catch (e: NonRetryableException) {
            Log.d(TAG, e.message)
            return Result.failure()
        } catch (e: AuthException) {
            Log.d(TAG, e.message)
            // TODO: Delete token
            return Result.failure()
        } catch (e: Exception) {
            Log.d(TAG, e.message)
            return Result.retry()
        }
    }

    private fun fetchUserTeams() {
        val fetchUserTeamWorks = mutableListOf<OneTimeWorkRequest>()
        CurrentUser.userTeams?.forEach {
            fetchUserTeamWorks.add(
                getWork<TeamWorker>(
                    workDataOf(
                        "type" to TeamWorkType.FETCH_TEAM.name,
                        "eventId" to it.key.toInt(),
                        "teamLeaderId" to it.value["leaderId"],
                        "isUserTeam" to true
                    )
                )
            )
        }

        val fetchChampionshipTeamWork = getWork<TeamWorker>(
            workDataOf(
                "type" to TeamWorkType.FETCH_BC_TEAM.name,
                "teamName" to CurrentUser.championshipTeamName
            )
        )
        val cleanupWork = getWork<TeamWorker>(
            workDataOf("type" to TeamWorkType.CLEAN_OLD_TEAMS.name)
        )
        if (fetchUserTeamWorks.isEmpty()) {
            WorkManager.getInstance().enqueueUniqueWork(
                "FETCH_ALL_USER_TEAMS",
                ExistingWorkPolicy.REPLACE,
                fetchChampionshipTeamWork
            )
        } else {
            WorkManager.getInstance().beginUniqueWork(
                "FETCH_ALL_USER_TEAMS",
                ExistingWorkPolicy.REPLACE,
                fetchUserTeamWorks
            )
                .then(fetchChampionshipTeamWork)
                .then(cleanupWork)
                .enqueue()
        }
    }
}