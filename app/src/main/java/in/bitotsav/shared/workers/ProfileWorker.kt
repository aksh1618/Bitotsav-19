package `in`.bitotsav.shared.workers

import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.data.User
import `in`.bitotsav.profile.data.UserRepository
import `in`.bitotsav.profile.utils.fetchPaymentDetailsAsync
import `in`.bitotsav.profile.utils.fetchProfileDetailsAsync
import `in`.bitotsav.shared.exceptions.AuthException
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.utils.getWork
import `in`.bitotsav.shared.workers.ProfileWorkType.FETCH_PAYMENT_DETAILS
import `in`.bitotsav.shared.workers.ProfileWorkType.valueOf
import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.get
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val TAG = "ProfileWorker"

enum class ProfileWorkType {
    FETCH_PROFILE,
    FETCH_PAYMENT_DETAILS,
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
                FETCH_PAYMENT_DETAILS -> {
                    val authToken = CurrentUser.authToken
                        ?: throw NonRetryableException("Auth token is empty")
                    runBlocking { fetchPaymentDetailsAsync(authToken).await() }
                    Log.d(TAG, "Payment details fetched")
                }
                ProfileWorkType.SET_USER -> {
                    val day1 = CurrentUser.paymentDetails?.get("day1") ?: false
                    val day2 = CurrentUser.paymentDetails?.get("day2") ?: false
                    val day3 = CurrentUser.paymentDetails?.get("day3") ?: false
                    val merchandise = CurrentUser.paymentDetails?.get("merchandise") ?: false
                    val accommodation = CurrentUser.paymentDetails?.get("accommodation") ?: false
                    val user = User(
                        CurrentUser.bitotsavId!!,
                        CurrentUser.name!!,
                        CurrentUser.email!!,
                        CurrentUser.championshipTeamName,
                        day1,
                        day2,
                        day3,
                        merchandise,
                        accommodation
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
        } catch (e: UnknownHostException) {
            Log.d(TAG, e.message ?: "Unknown Error")
            return Result.failure()
        } catch (e: SocketTimeoutException) {
            Log.d(TAG, e.message ?: "Unknown Error")
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
        val fetchPaymentDetailsWork = getWork<ProfileWorker>(
            workDataOf("type" to ProfileWorkType.FETCH_PAYMENT_DETAILS.name)
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
                .then(fetchPaymentDetailsWork)
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
                .then(fetchPaymentDetailsWork)
                .then(setUserWork)
                .enqueue()
        }
    }
}