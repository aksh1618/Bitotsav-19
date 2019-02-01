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

class ProfileWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    override fun doWork(): Result {
        try {
            val type = inputData.getString("type")?.let { valueOf(it) }
                ?: return Result.failure(workDataOf("Error" to "Invalid work type"))
            val authToken = CurrentUser.authToken
                ?: return Result.failure(workDataOf("Error" to "Auth token is empty"))
            runBlocking { fetchProfileDetailsAsync(authToken).await() }
            Log.d(TAG, "Fetching completed")

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
            val cleanupWork = getWork<TeamWorker>(
                workDataOf("type" to TeamWorkType.CLEAN_OLD_TEAMS.name)
            )
            WorkManager.getInstance().beginWith(fetchUserTeamWorks)/*.then(cleanupWork)*/.enqueue()
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
}