package `in`.bitotsav.teams.utils

import `in`.bitotsav.profile.utils.AuthException
import `in`.bitotsav.teams.api.TeamRegistrationService
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val TAG = "TeamRegistrationUtils"

data class Member(
    val memberId: String,
    val memberEmail: String
)

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
    bitId: String,
    members: List<Member>
): Deferred<Boolean> {
    return CoroutineScope(Dispatchers.Main).async {
        val body = mapOf(
            "eventId" to eventId,
            "leaderId" to bitId,
            "members" to members.toString()
        )
        val authHeaderValue = "Authorization $authToken"
        val request
                = TeamRegistrationService.api.registerForEventAsync(authHeaderValue, body)
        val response = request.await()
        if (response.code() == 200) {
            Log.d(TAG, "Registered for event $eventId")
            return@async true
        } else {
            when (response.code()) {
                404 -> throw Exception("Incorrect Bitotsav id and/or email id")
                405 -> throw Exception("All members don't belong to the same college")
                409 -> throw Exception("One or more members are registered for the event")
                else -> throw Exception("Server is currently facing some issues. Try again later")
            }
        }
    }
}

//GET - /eventDeregistration/:eventId/:bitId - headers: {token:"Authorization <token_value>"}
//502 - Server error
//403 - Not registered or not the team leader
//200 - Success
fun deregisterForEventAsync(
    authToken: String,
    eventId: Int,
    bitId: String
): Deferred<Boolean> {
    return CoroutineScope(Dispatchers.Main).async {
        val authHeaderValue = "Authorization $authToken"
        val request
                = TeamRegistrationService.api.deregisterForEventAsync(
            authHeaderValue,
            eventId,
            bitId.substringAfter("/")
        )
        val response = request.await()
        if (response.code() == 200) {
            Log.d(TAG, "De-registered for event $eventId")
            return@async true
        } else {
            when (response.code()) {
                403 -> throw Exception("Not registered for this event or not the team leader")
                else -> throw Exception("Server is currently facing some issues. Try again later")
            }
        }
    }
}

//POST - /championship - headers: {token:"Authorization <token_value>"}
// - body: {teamMembers[] {memberId, memberEmail}, teamName}
//403 - Auth failure
//502 - Server error
//404 - Incorrect Bitotsav ID or email Id
//405 - All members don't belong to same college
//409 - Team name already taken or Some member is already registered for the event
//200 - Success
//TODO: - Incorrect errors
fun registerForChampionshipAsync(
    authToken: String,
    teamName: String,
    members: List<Member>
): Deferred<Boolean> {
    return CoroutineScope(Dispatchers.Main).async {
        val authHeaderValue = "Authorization $authToken"
        val body = mapOf(
            "teamName" to teamName,
            "teamMembers" to members.toString()
        )
        val request
                = TeamRegistrationService.api.registerForChampionshipAsync(authHeaderValue, body)
        val response = request.await()
        if (response.code() == 200) {
            Log.d(TAG, "Registered for Bitotsav championship")
            return@async true
        } else {
            when (response.code()) {
                403 -> throw AuthException("Authentication failed")
                404 -> throw Exception("Incorrect Bitotsav id and/or email id")
                405 -> throw Exception("All members don't belong to the same college")
                409 -> throw Exception("Team name already taken or one or more members are already registered")
                else -> throw Exception("Server is currently facing some issues. Try again later")
            }
        }
    }
}