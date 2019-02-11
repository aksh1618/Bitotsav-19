package `in`.bitotsav.notification.utils

import `in`.bitotsav.notification.fcm.api.FcmTokenService
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.shared.exceptions.NetworkException
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.utils.getWorkNameForFcmTokenWorker
import `in`.bitotsav.shared.utils.scheduleUniqueWork
import `in`.bitotsav.shared.utils.scheduleWork
import `in`.bitotsav.shared.workers.FcmTokenWorkType
import `in`.bitotsav.shared.workers.FcmTokenWorker
import android.util.Log
import androidx.work.workDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val TAG = "FcmUtils"

//POST - /addFCMToken - headers: {token:"Authorization <token_value>"} - body: {token}
//403 - Token (to be added) not sent or auth failure
//502 - Server error
//200 - Success
fun sendFcmTokenAsync(authToken: String, fcmToken: String): Deferred<Any> {
    return CoroutineScope(Dispatchers.IO).async {
        val body = mapOf("token" to fcmToken)
        val authHeaderValue = "Authorization $authToken"
        val request = FcmTokenService.api.addFcmTokenAsync(authHeaderValue, body)
        val response = request.await()
        if (response.code() == 200) {
            Log.d(TAG, "Fcm token sent to server")
            return@async true
        } else {
            when (response.code()) {
                403 -> throw NonRetryableException("Fcm token missing or user authentication failed")
                409 -> throw NonRetryableException("Token already exists")
                else -> throw NetworkException("Unable to send token to server. Response code: ${response.code()}")
            }
        }
    }
}

//POST - /removeFCMToken - headers: {token:"Authorization <token_value>"} - body: {token}
//403 - Token (to be removed) not sent or auth failure
//502 - Server error
//404 - Token (to be removed) not found
//200 - Success
fun deleteFcmTokenAsync(authToken: String, fcmToken: String): Deferred<Any> {
    return CoroutineScope(Dispatchers.IO).async {
        val body = mapOf("token" to fcmToken)
        val authHeaderValue = "Authorization $authToken"
        val request = FcmTokenService.api.removeFcmTokenAsync(authHeaderValue, body)
        val response = request.await()
        if (response.code() == 200) {
            Log.d(TAG, "Fcm token deleted from server")
            return@async true
        } else {
            when (response.code()) {
                403 -> throw NonRetryableException("Fcm token missing or user authentication failed")
                404 -> throw NonRetryableException("Token not found")
                else -> throw NetworkException("Unable to send token to server. Response code: ${response.code()}")
            }
        }
    }
}

fun sendFcmTokenToServer() {
    val authToken = CurrentUser.authToken
    val fcmToken = CurrentUser.fcmToken
    if (authToken == null || fcmToken == null) {
        Log.wtf(TAG, "Token is missing!")
        return
    }
    scheduleUniqueWork<FcmTokenWorker>(
        workDataOf(
            "type" to FcmTokenWorkType.SEND_TOKEN.name,
            "authToken" to authToken,
            "fcmToken" to fcmToken
        ),
        getWorkNameForFcmTokenWorker(FcmTokenWorkType.SEND_TOKEN)
    )
}

fun deleteFcmTokenFromServer() {
    val authToken = CurrentUser.authToken
    val fcmToken = CurrentUser.fcmToken
    if (authToken == null || fcmToken == null) {
        Log.wtf(TAG, "Token is missing!")
        return
    }
    scheduleWork<FcmTokenWorker>(
        workDataOf(
            "type" to FcmTokenWorkType.DELETE_TOKEN.name,
            "authToken" to authToken,
            "fcmToken" to fcmToken
        )
    )
}