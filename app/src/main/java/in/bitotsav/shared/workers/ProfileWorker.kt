package `in`.bitotsav.shared.workers

import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.data.User
import `in`.bitotsav.profile.data.UserRepository
import `in`.bitotsav.profile.utils.fetchProfileDetailsAsync
import `in`.bitotsav.shared.exceptions.AuthException
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.utils.getWork
import `in`.bitotsav.shared.workers.ProfileWorkType.valueOf
import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.get

private const val TAG = "ProfileWorker"

enum class ProfileWorkType {
    FETCH_PROFILE,
    SET_USER
}

class ProfileWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        try {
            val type = inputData.getString("type")?.let { valueOf(it) }
                ?: throw NonRetryableException("Invalid work type")
            when (type) {
                ProfileWorkType.FETCH_PROFILE -> {
                    val authToken = CurrentUser.authToken
                        ?: throw NonRetryableException("Auth token is empty")
                    runBlocking { fetchProfileDetailsAsync(authToken).await() }
                    Log.d(TAG, "Fetching completed")
                    fetchUserTeams()
                }
                ProfileWorkType.SET_USER -> {
                    val user = User(
                        CurrentUser.bitotsavId!!,
                        CurrentUser.name!!,
                        CurrentUser.email!!,
                        CurrentUser.championshipTeamName
                    )
                    runBlocking {
                        get().koin.get<UserRepository>().insert(user)
                        Log.d(TAG, "User inserted into DB")
                    }
                }
            }

            return Result.success()
        } catch (e: NonRetryableException) {
            Log.d(TAG, e.message ?: "Non-retryable exception")
            return Result.failure()
        } catch (e: AuthException) {
            Log.d(TAG, e.message ?: "Authentication exception")
            // TODO: Delete token
            return Result.failure()
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Unknown Error")
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
        val setUserWork = getWork<ProfileWorker>(
            workDataOf("type" to ProfileWorkType.SET_USER.name)
        )
        if (fetchUserTeamWorks.isEmpty()) {
            WorkManager.getInstance().beginUniqueWork(
                "FETCH_ALL_USER_TEAMS",
                ExistingWorkPolicy.REPLACE,
                fetchChampionshipTeamWork
            )
                .then(cleanupWork)
                .then(setUserWork)
                .enqueue()
        } else {
            WorkManager.getInstance().beginUniqueWork(
                "FETCH_ALL_USER_TEAMS",
                ExistingWorkPolicy.REPLACE,
                fetchUserTeamWorks
            )
                .then(fetchChampionshipTeamWork)
                .then(cleanupWork)
                .then(setUserWork)
                .enqueue()
        }
    }
}