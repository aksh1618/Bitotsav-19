package `in`.bitotsav.events.utils

import `in`.bitotsav.teams.api.TeamRegistrationService
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val TAG = "EventRegistrationUtils"

data class Member(val memberId: String, val memberEmail: String)

//POST - /eventRegistration - headers: {token:"Authorization <token_value>"}
// - body: {members[] {memberId, memberEmail}, eventId, leaderId}
//502 - Server error
//404 - Incorrect Bitotsav ID or email Id
//405 - All members don't belong to same college
//409 - Some member is already registered for the event
//200 - Success
fun registerForEventAsync(
    authToken: String,
    eventId: Int,
    bitotsavId: String,
    members: List<Member> // All except logged in one
) = CoroutineScope(Dispatchers.Main).async {
    val body = mapOf(
        "eventId" to eventId,
        "leaderId" to bitotsavId,
        "members" to Gson().toJson(members)
    )
    val authHeaderValue = "Authorization $authToken"
    val request = TeamRegistrationService.api.registerForEventAsync(authHeaderValue, body)
    val response = request.await()
    Log.d(TAG, "Response Code: ${response.code()}")
    when (response.code()) {
        200 -> run { Log.d(TAG, "Registered for event $eventId") }
        404 -> throw Exception("Incorrect Bitotsav id and/or email id")
        405 -> throw Exception("All members don't belong to the same college")
        408 -> throw Exception("Registration for this event has been closed")
        409 -> throw Exception("One or more members are registered for the event")
        else -> throw Exception("Server is currently facing some issues. Try again later")
    }
}

//GET - /eventDeregistration/:eventId/:bitId - headers: {token:"Authorization <token_value>"}
//502 - Server error
//403 - Not registered or not the team leader
//200 - Success
fun deregisterForEventAsync(
    authToken: String,
    eventId: Int,
    bitotsavId: String
) = CoroutineScope(Dispatchers.Main).async {
    val authHeaderValue = "Authorization $authToken"
    val request = TeamRegistrationService.api.deregisterForEventAsync(
        authHeaderValue,
        eventId,
        bitotsavId.substringAfter("/")
    )
    val response = request.await()
    Log.d(TAG, "Response Code: ${response.code()}")
    when (response.code()) {
        200 -> run { Log.d(TAG, "De-registered for event $eventId") }
        403 -> throw Exception("Not registered for this event or not the team leader")
        408 -> throw Exception("De-registration for this event has been closed")
        else -> throw Exception("Server is currently facing some issues. Try again later")
    }
}