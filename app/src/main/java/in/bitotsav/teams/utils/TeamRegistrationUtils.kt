package `in`.bitotsav.teams.utils

import `in`.bitotsav.profile.utils.AuthException
import `in`.bitotsav.teams.api.TeamRegistrationService
import `in`.bitotsav.teams.data.Member
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val TAG = "TeamRegistrationUtils"

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
            "teamMembers" to Gson().toJson(members)
        )
        val request =
            TeamRegistrationService.api.registerForChampionshipAsync(authHeaderValue, body)
        val response = request.await()
        if (response.code() == 200) {
            Log.d(TAG, "Registered for Bitotsav championship")
            return@async true
        } else {
            Log.d(TAG, "Response Code: ${response.code()}")
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