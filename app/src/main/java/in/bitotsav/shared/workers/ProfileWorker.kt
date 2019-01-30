package `in`.bitotsav.shared.workers

import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.utils.fetchProfileDetailsAsync
import `in`.bitotsav.shared.workers.ProfileWorkType.valueOf
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking

private const val TAG = "ProfileWorker"

enum class ProfileWorkType {
    FETCH_PROFILE,
}

class ProfileWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    override fun doWork(): Result {

        return runBlocking {
            try {
                val type = inputData.getString("type")?.let { valueOf(it) }
                    ?: return@runBlocking Result.failure(workDataOf("Error" to "Invalid work type"))
                val authToken = CurrentUser.authToken
                    ?: return@runBlocking Result.failure(workDataOf("Error" to "Auth token is empty"))
                fetchProfileDetailsAsync(authToken).await()
                Log.d("ProfileWorker", "Fetching completed")
                return@runBlocking Result.success()
            } catch (e: Exception) {
                Log.d(TAG, e.message)
                return@runBlocking Result.retry()
            }
        }
    }
}