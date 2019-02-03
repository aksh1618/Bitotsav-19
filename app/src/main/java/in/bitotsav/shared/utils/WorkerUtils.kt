package `in`.bitotsav.shared.utils

import `in`.bitotsav.shared.workers.*
import androidx.work.*
import java.util.concurrent.TimeUnit

const val delay = 0L

inline fun <reified T : Worker> scheduleWork(input: Data) {
    val constraints: Constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()
    val oneTimeWorkRequest = OneTimeWorkRequest.Builder(T::class.java)
        .setInputData(input)
        .setInitialDelay(delay, TimeUnit.SECONDS)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 20, TimeUnit.SECONDS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance().enqueue(oneTimeWorkRequest)
}

inline fun <reified T : Worker> scheduleUniqueWork(input: Data, uniqueWorkName: String) {
    val constraints: Constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()
    val oneTimeWorkRequest = OneTimeWorkRequest.Builder(T::class.java)
        .setInputData(input)
        .setInitialDelay(delay, TimeUnit.SECONDS)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 20, TimeUnit.SECONDS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance().enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
}

inline fun <reified T : Worker> getWork(input: Data): OneTimeWorkRequest {
    val constraints: Constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()
    return OneTimeWorkRequest.Builder(T::class.java)
        .setInputData(input)
        .setInitialDelay(delay, TimeUnit.SECONDS)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 20, TimeUnit.SECONDS)
        .setConstraints(constraints)
        .build()
}

fun getWorkNameForEventWorker(type: EventWorkType, eventId: Int? = null): String {
    when (type) {
        EventWorkType.FETCH_EVENT -> {
            eventId?.let {
                return (type.name + eventId.toString())
            }
            throw IllegalArgumentException("Event id not found")
        }
        EventWorkType.FETCH_ALL_EVENTS -> return type.name
    }
}

fun getWorkNameForFcmTokenWorker(type: FcmTokenWorkType): String {
    when (type) {
        FcmTokenWorkType.SEND_TOKEN -> return type.name
        FcmTokenWorkType.DELETE_TOKEN -> throw IllegalArgumentException("No unique work for this type")
    }
}

fun getWorkNameForFeedWorker(type: FeedWorkType): String {
    return type.name
}

fun getWorkNameForProfileWorker(type: ProfileWorkType): String {
    return type.name
}

fun getWorkNameForResultWorker(type: ResultWorkType, eventId: Int? = null): String {
    when (type) {
        ResultWorkType.RESULT -> {
            eventId?.let {
                return (type.name + eventId.toString())
            }
            throw IllegalArgumentException("Event id not found")
        }
        ResultWorkType.WINNING_TEAMS -> return type.name
    }
}

fun getWorkNameForTeamWorker(
    type: TeamWorkType,
    championshipTeamName: String? = null,
    eventId: Int? = null,
    teamLeaderId: String? = null,
    isUserTeam: Boolean? = null
): String {
    when (type) {
        TeamWorkType.FETCH_TEAM -> {
            if (eventId == null || teamLeaderId == null || isUserTeam == null) {
                throw IllegalArgumentException("All required parameters not passed")
            }
            return (type.name + eventId.toString() + teamLeaderId + isUserTeam.toString())
        }
        TeamWorkType.FETCH_BC_TEAM -> {
            championshipTeamName?.let {
                return (type.name + championshipTeamName)
            }
            throw IllegalArgumentException("Team name not found")
        }
        TeamWorkType.FETCH_ALL_TEAMS -> return type.name
        TeamWorkType.CLEAN_OLD_TEAMS -> return type.name
    }
}