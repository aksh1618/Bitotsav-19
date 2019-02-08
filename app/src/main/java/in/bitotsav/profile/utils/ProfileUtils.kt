package `in`.bitotsav.profile.utils

import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.api.ProfileService
import `in`.bitotsav.shared.exceptions.AuthException
import `in`.bitotsav.shared.exceptions.NetworkException
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.utils.getWork
import `in`.bitotsav.shared.utils.getWorkNameForProfileWorker
import `in`.bitotsav.shared.workers.ProfileWorkType
import `in`.bitotsav.shared.workers.ProfileWorker
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlin.collections.set

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
//            ("Store participant details here")
//            "teamMembers": [
//            {
//                "name": $name,
//                "email": "$email,
//                "id": $id
//            }]
            val name = response.body()?.get("name")?.toString()
            val email = response.body()?.get("email")?.toString()
            val bitotsavId = response.body()?.get("id")?.toString()
            var teamName = response.body()?.get("teamName")?.toString()
            if (name == null || email == null || bitotsavId == null || teamName == null) {
                throw NonRetryableException("API not functioning correctly. Null fields")
            }
            if ("-1" == teamName) teamName = null
            CurrentUser.name = name
            CurrentUser.email = email
            CurrentUser.bitotsavId = bitotsavId
            CurrentUser.championshipTeamName = teamName

            val teamMembers = mutableMapOf<String, Map<String, String>>()
            teamName?.let {
                val members = response.body()?.get("teamMembers") as List<Map<String, String>>
                members.forEach {
                    val bitId = it["id"].toString()
                    val memberName = it["name"].toString()
                    val memberEmail = it["email"].toString()
                    teamMembers[bitId] = mapOf("name" to memberName, "email" to memberEmail)
                }
                CurrentUser.teamMembers = teamMembers.toMap()
            }

            val teams = response.body()?.get("events") as List<Map<String, Any>>
            val userTeams = mutableMapOf<String, Map<String, String>>()
            teams.forEach {
                val eventId = (it["eventId"] as Double).toInt()
                userTeams[eventId.toString()] =
                    mapOf("leaderId" to it["teamLeader"].toString())
            }
            CurrentUser.userTeams = userTeams.toMap()
            Log.d(TAG, "Participant details stored in CurrentUser")
            Log.d(TAG, CurrentUser.toString())
        } else {
            when (response.code()) {
                403 -> throw AuthException("Authentication error")
                404 -> throw NonRetryableException("Participant not found")
                else -> throw NetworkException("Unable to fetch the participant. Response code: ${response.code()}")
            }
        }
    }
}

fun syncUserProfile(): ListenableFuture<Operation.State.SUCCESS> {

    val profileWork = getWork<ProfileWorker>(
        workDataOf("type" to ProfileWorkType.FETCH_PROFILE.name)
    )

    return WorkManager.getInstance().enqueueUniqueWork(
        getWorkNameForProfileWorker(ProfileWorkType.FETCH_PROFILE),
        ExistingWorkPolicy.REPLACE,
        profileWork
    ).result
}

fun syncUserAndRun(block: () -> Unit) {
    // FIXME: Improvement Possible?.
    syncUserProfile().addListener(
        { block.invoke() },
        {
            // Give harbi's slow ass method some time to complete
            Handler(Looper.getMainLooper()).postDelayed(it, 1000)
        }
    )
}