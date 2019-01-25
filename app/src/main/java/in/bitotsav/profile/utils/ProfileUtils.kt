package `in`.bitotsav.profile.utils

import `in`.bitotsav.profile.api.ProfileService
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val TAG = "ProfileUtils"

//GET - /getParticipantDetails - headers: {token:"Authorization <token_value>"}
//502 - Server error
//404 - Participant not found
//200 - Participant details as an object in {data}
fun fetchProfileDetailsAsync(authToken: String): Deferred<Any> {
    return CoroutineScope(Dispatchers.IO).async {
        val authHeaderValue = "Authorization $authToken"
        val request = ProfileService.api.getParticipantDetailsAsync(authHeaderValue)
        val response = request.await()
        if (response.code() == 200) {
            Log.d(TAG, "Participant details received from server")
            TODO("Store participant details here")
        } else {
            when (response.code()) {
                403 -> throw AuthException("Authentication error")
                404 -> throw Exception("Participant not found")
                else -> throw Exception("Unable to fetch the participant")
            }
        }
    }
}