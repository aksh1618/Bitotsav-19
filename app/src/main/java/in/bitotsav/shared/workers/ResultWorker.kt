package `in`.bitotsav.shared.workers

import `in`.bitotsav.HomeActivity
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.notification.utils.Channel
import `in`.bitotsav.notification.utils.displayNotification
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.teams.nonchampionship.data.NonChampionshipTeamRepository
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.get

private const val TAG = "ResultWorker"

enum class ResultWorkType {
    RESULT
}

class ResultWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    override fun doWork(): Result {

        return runBlocking {
            try {
                val eventId = inputData.getInt("eventId", -1)
                if (eventId == -1)
                    return@runBlocking Result.failure(workDataOf("Error" to "Event id is empty"))
                val event = get<EventRepository>().getById(eventId)
//                TODO("Check winner here and update or fetch teams")
                val position1Task = event?.position1?.get("teamLeader")?.let {
                    get<NonChampionshipTeamRepository>()
                        .fetchNonChampionshipTeamAsync(eventId, it, false)
                }
                val position2Task = event?.position2?.get("teamLeader")?.let {
                    get<NonChampionshipTeamRepository>()
                        .fetchNonChampionshipTeamAsync(eventId, it, false)
                }
                val position3Task = event?.position3?.get("teamLeader")?.let {
                    get<NonChampionshipTeamRepository>()
                        .fetchNonChampionshipTeamAsync(eventId, it, false)
                }
                position1Task?.await()
                position2Task?.await()
                position3Task?.await()
                val leaderId = CurrentUser.userTeams?.get(eventId.toString())
                val position: String?
//                TODO("Pass appropriate intent")
                val intent = Intent(applicationContext, HomeActivity::class.java)
                leaderId?.get("leaderId")?.let {
                    position = when (it) {
                        event?.position1?.get("teamLeader") -> "1st"
                        event?.position2?.get("teamLeader") -> "2nd"
                        event?.position3?.get("teamLeader") -> "3rd"
                        else -> null
                    }
                    position?.let {
                        val eventName = get<EventRepository>().getNameById(eventId)
                        val content = "Your team secured $position position in $eventName"
                        displayNotification(
                            "Congratulations!",
                            content,
                            System.currentTimeMillis(),
                            Channel.PM,
                            intent,
                            applicationContext
                        )
                    }
                }
                Log.d(TAG, "Analysing winners for event: $eventId")
                return@runBlocking Result.success()
            } catch (e: Exception) {
                Log.d(TAG, e.message)
                return@runBlocking Result.retry()
            }
        }
    }
}