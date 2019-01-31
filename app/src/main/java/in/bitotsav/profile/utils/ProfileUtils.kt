package `in`.bitotsav.profile.utils

import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.api.ProfileService
import `in`.bitotsav.shared.network.getWork
import `in`.bitotsav.shared.workers.ProfileWorkType
import `in`.bitotsav.shared.workers.ProfileWorker
import `in`.bitotsav.shared.workers.TeamWorkType
import `in`.bitotsav.shared.workers.TeamWorker
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
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
//            TODO("Store participant details here")
            CurrentUser.name = response.body()?.get("name")?.toString()
            CurrentUser.email = response.body()?.get("email")?.toString()
            CurrentUser.bitotsavId = response.body()?.get("id")?.toString()
            var teamName = response.body()?.get("teamName")?.toString()
            if ("-1" == teamName) teamName = null
            CurrentUser.championshipTeamName = teamName
//            TODO: Store user teams
            val teams = response.body()?.get("events") as List<Map<String, Any>>
            val userTeams = mutableMapOf<String, Map<String, String>>()
            teams.forEach {
                val eventId = (it["eventId"] as Double).toInt()
                userTeams[eventId.toString()] =
                    mapOf("leaderId" to it["teamLeader"].toString())
            }
            CurrentUser.userTeams = userTeams.toMap()
        } else {
            when (response.code()) {
                403 -> throw AuthException("Authentication error")
                404 -> throw Exception("Participant not found")
                else -> throw Exception("Unable to fetch the participant")
            }
        }
    }
}

fun syncUserProfile(): ListenableFuture<Operation.State.SUCCESS> {
    val listOfWorks = mutableListOf<OneTimeWorkRequest>()
    CurrentUser.userTeams?.forEach {
        listOfWorks.add(
            getWork<TeamWorker>(
                workDataOf(
                    "type" to TeamWorkType.FETCH_TEAM.name,
                    "teamLeaderId" to it.value["teamLeaderId"],
                    "isUserTeam" to true
                )
            )
        )
    }
    val profileWork = getWork<ProfileWorker>(
        workDataOf("type" to ProfileWorkType.FETCH_PROFILE.name)
    )
    val cleanupWork = getWork<TeamWorker>(
        workDataOf("type" to TeamWorkType.CLEAN_OLD_TEAMS.name)
    )
    // TODO: [FIXME] @ ashank
    return WorkManager.getInstance()
        .beginWith(profileWork)/*.then(listOfWorks).then(cleanupWork)*/.enqueue().result
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