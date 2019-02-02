package `in`.bitotsav.shared.workers

import `in`.bitotsav.HomeActivity
import `in`.bitotsav.events.data.Event
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.notification.utils.Channel
import `in`.bitotsav.notification.utils.displayNotification
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.utils.forEachParallel
import `in`.bitotsav.shared.workers.ResultWorkType.valueOf
import `in`.bitotsav.teams.nonchampionship.data.NonChampionshipTeamRepository
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.get

private const val TAG = "ResultWorker"

enum class ResultWorkType {
    RESULT,
    WINNING_TEAMS
}

class ResultWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    override fun doWork(): Result {
        try {
            val type = inputData.getString("type")?.let { valueOf(it) }
                ?: throw NonRetryableException("Invalid work type")
            when (type) {
                ResultWorkType.RESULT -> {
                    val eventId = inputData.getInt("eventId", -1)
                    if (eventId == -1)
                        throw NonRetryableException("Event id is empty")
                    val event = runBlocking { get<EventRepository>().getById(eventId) }
                        ?: throw NonRetryableException("Event $eventId not found in DB")
                    fetchWinningTeamsByEvent(event)
                    checkAndHandleIfUsersTeam(event)
                }
                ResultWorkType.WINNING_TEAMS -> {
                    val events = runBlocking { get<EventRepository>().getAllEvents() }
                    events.forEachParallel {
                        fetchWinningTeamsByEvent(it)
                    }
                    Log.d(TAG, "All winning teams retrieved.")
                }
            }
            return Result.success()
        } catch (e: NonRetryableException) {
            Log.d(TAG, e.message)
            return Result.failure()
        } catch (e: Exception) {
            Log.d(TAG, e.message)
            return Result.retry()
        }
    }

    private fun fetchWinningTeamsByEvent(event: Event) {
        val position1Task = event.position1?.get("teamLeader")?.let {
            get<NonChampionshipTeamRepository>()
                .fetchNonChampionshipTeamAsync(event.id, it, false)
        }
        val position2Task = event.position2?.get("teamLeader")?.let {
            get<NonChampionshipTeamRepository>()
                .fetchNonChampionshipTeamAsync(event.id, it, false)
        }
        val position3Task = event.position3?.get("teamLeader")?.let {
            get<NonChampionshipTeamRepository>()
                .fetchNonChampionshipTeamAsync(event.id, it, false)
        }
        runBlocking {
            position1Task?.await()
            position2Task?.await()
            position3Task?.await()
        }
    }

    private fun checkAndHandleIfUsersTeam(event: Event) {
        val leaderId = CurrentUser.userTeams?.get(event.id.toString())
        val position: String?
//                TODO("Pass appropriate intent")
        val intent = Intent(applicationContext, HomeActivity::class.java)
        Log.d(TAG, "Analysing winners for event: $event.id")
        leaderId?.get("leaderId")?.let {
            position = when (it) {
                event.position1?.get("teamLeader") -> "1st"
                event.position2?.get("teamLeader") -> "2nd"
                event.position3?.get("teamLeader") -> "3rd"
                else -> null
            }
            position?.let {
                val eventName = runBlocking { get<EventRepository>().getNameById(event.id) }
                    ?: throw NonRetryableException("Event name not found for: ${event.id}")
                val title = "Congratulations!"
                val content = "Your team secured $position position in $eventName."
                displayNotification(
                    title,
                    content,
                    System.currentTimeMillis(),
                    Channel.PM,
                    intent,
                    applicationContext
                )
            }
        }
    }
}