package `in`.bitotsav.shared.workers

import `in`.bitotsav.notification.utils.deleteFcmTokenAsync
import `in`.bitotsav.notification.utils.sendFcmTokenAsync
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.shared.workers.FcmTokenWorkType.valueOf
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking

private const val TAG = "FcmTokenWorker"

enum class FcmTokenWorkType {
    SEND_TOKEN,
    DELETE_TOKEN
}

class FcmTokenWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    override fun doWork(): Result {

        return runBlocking {
            try {
                val type = inputData.getString("type")?.let { valueOf(it) }
                    ?: return@runBlocking Result.failure(workDataOf("Error" to "Invalid work type"))
                val authToken = CurrentUser.authToken
                    ?: return@runBlocking Result.failure(workDataOf("Error" to "Auth token is empty"))
                val fcmToken = CurrentUser.fcmToken
                    ?: return@runBlocking Result.failure(workDataOf("Error" to "FCM token not found"))
                when (type) {
                    FcmTokenWorkType.SEND_TOKEN -> sendFcmTokenAsync(authToken, fcmToken).await()
                    FcmTokenWorkType.DELETE_TOKEN -> deleteFcmTokenAsync(authToken, fcmToken).await()
                }
                return@runBlocking Result.success()
            } catch (e: Exception) {
                Log.d(TAG, e.message)
                return@runBlocking Result.retry()
            }
        }
    }
}