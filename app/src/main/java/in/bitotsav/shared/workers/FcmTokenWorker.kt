package `in`.bitotsav.shared.workers

import `in`.bitotsav.notification.utils.deleteFcmTokenAsync
import `in`.bitotsav.notification.utils.sendFcmTokenAsync
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.workers.FcmTokenWorkType.valueOf
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking

private const val TAG = "FcmTokenWorker"

enum class FcmTokenWorkType {
    SEND_TOKEN,
    DELETE_TOKEN
}

class FcmTokenWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        try {
            val type = inputData.getString("type")?.let { valueOf(it) }
                ?: throw NonRetryableException("Invalid work type")
            val authToken = inputData.getString("authToken")
                ?: throw NonRetryableException("Auth token is empty")
            val fcmToken = inputData.getString("fcmToken")
                ?: throw NonRetryableException("FCM token not found")
            when (type) {
                FcmTokenWorkType.SEND_TOKEN -> runBlocking { sendFcmTokenAsync(authToken, fcmToken).await() }
                FcmTokenWorkType.DELETE_TOKEN -> runBlocking { deleteFcmTokenAsync(authToken, fcmToken).await() }
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