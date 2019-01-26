package `in`.bitotsav.shared.workers

import `in`.bitotsav.profile.utils.fetchProfileDetailsAsync
import `in`.bitotsav.shared.workers.ProfileWorkType.valueOf
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking

private const val TAG = "ProfileWorker"

enum class ProfileWorkType {
    FETCH_PROFILE,
}

class ProfileWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    override fun doWork(): Result {
        val type = valueOf(inputData.getString("type")!!)
        val authToken = "" //TODO: Get from shared Prefs

        return runBlocking {
            try {
                fetchProfileDetailsAsync(authToken).await()
                return@runBlocking Result.success()
            } catch (e: Exception) {
                Log.d(TAG, e.message)
                return@runBlocking Result.retry()
            }
        }
    }
}