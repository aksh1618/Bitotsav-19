package `in`.bitotsav.notification.utils

import `in`.bitotsav.notification.fcm.api.FcmTokenService
import `in`.bitotsav.profile.utils.AuthException
import `in`.bitotsav.shared.network.NetworkException
import android.util.Log
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
            Log.d(TAG, "Fcm token send to server")
            return@async true
        } else {
            when (response.code()) {
//                TODO("Delete local token for 403")
                403 -> throw AuthException("Fcm token missing or User authentication failed")
                409 -> throw Exception("Token already exists")
                else -> throw NetworkException("Unable to send token to server")
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
//                TODO("Delete local token for 403")
                403 -> throw AuthException("Fcm token missing or User authentication failed")
                404 -> throw Exception("Token not found")
                else -> throw NetworkException("Unable to send token to server")
            }
        }
    }
}